from __future__ import annotations

from contextlib import asynccontextmanager
from typing import Any

from fastapi import FastAPI, Request, Response, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

from cnn_predict import (
    PredictorInputError,
    load_label_mapping,
    load_ticket_model,
    predict_ticket,
    resolve_id_to_label,
)


SERVICE_NAME = "cnn-text-prediction-service"
TOP_PREDICTIONS_LIMIT = 3


class PredictRequest(BaseModel):
    text: str


class TopPredictionResponse(BaseModel):
    category: str
    probability: float


class PredictResponse(BaseModel):
    predictedCategory: str
    confidence: float
    topPredictions: list[TopPredictionResponse]


class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
    labelsLoaded: bool
    service: str
    details: list[str] | None = None


class ErrorResponse(BaseModel):
    error: str
    message: str


def build_error_response(status_code: int, error: str, message: str) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        content=ErrorResponse(error=error, message=message).model_dump(),
    )


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.model_loaded = False
    app.state.labels_loaded = False
    app.state.startup_errors = []

    try:
        load_ticket_model()
        app.state.model_loaded = True
    except Exception as exception:
        app.state.startup_errors.append(f"Model load failed: {exception}")

    try:
        resolve_id_to_label(load_label_mapping())
        app.state.labels_loaded = True
    except Exception as exception:
        app.state.startup_errors.append(f"Label mapping load failed: {exception}")

    yield


app = FastAPI(title="CNN Text Prediction Service", lifespan=lifespan)


@app.exception_handler(RequestValidationError)
async def request_validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    return build_error_response(
        status.HTTP_400_BAD_REQUEST,
        "Bad Request",
        "Request body is invalid. Expected JSON with a non-empty 'text' field.",
    )


@app.exception_handler(Exception)
async def unexpected_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    return build_error_response(
        status.HTTP_500_INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "Prediction service encountered an unexpected error.",
    )


def _service_is_ready() -> bool:
    return bool(app.state.model_loaded and app.state.labels_loaded)


def _health_payload() -> HealthResponse:
    return HealthResponse(
        status="OK" if _service_is_ready() else "NOT_READY",
        modelLoaded=bool(app.state.model_loaded),
        labelsLoaded=bool(app.state.labels_loaded),
        service=SERVICE_NAME,
        details=list(app.state.startup_errors) or None,
    )


@app.get(
    "/health",
    response_model=HealthResponse,
    responses={503: {"model": HealthResponse}},
)
async def health(response: Response) -> HealthResponse:
    payload = _health_payload()
    if payload.status != "OK":
        response.status_code = status.HTTP_503_SERVICE_UNAVAILABLE
    return payload


@app.post(
    "/predict",
    response_model=PredictResponse,
    responses={
        400: {"model": ErrorResponse},
        500: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
    },
)
async def predict(request: PredictRequest) -> PredictResponse | JSONResponse:
    if not _service_is_ready():
        return build_error_response(
            status.HTTP_503_SERVICE_UNAVAILABLE,
            "Service Unavailable",
            "Prediction service is not ready. Model or label mapping failed to load.",
        )

    if not request.text.strip():
        return build_error_response(
            status.HTTP_400_BAD_REQUEST,
            "Bad Request",
            "Input text must not be empty or whitespace only.",
        )

    try:
        prediction: dict[str, Any] = predict_ticket(request.text, top_k=TOP_PREDICTIONS_LIMIT)
    except PredictorInputError as exception:
        return build_error_response(status.HTTP_400_BAD_REQUEST, "Bad Request", str(exception))
    except Exception:
        return build_error_response(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "Prediction failed due to an internal service error.",
        )

    return PredictResponse(**prediction)
