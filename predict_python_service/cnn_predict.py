from __future__ import annotations

import json
import re
from functools import lru_cache
from pathlib import Path
from typing import Any

import tensorflow as tf


BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / "models"
MODEL_PATH = MODELS_DIR / "cnn_text_model.keras"
LABEL_MAPPING_PATH = MODELS_DIR / "label_mapping.json"


class PredictorInputError(ValueError):
    pass


def clean_text(text: str) -> str:
    text = str(text).lower()
    text = re.sub(r"https?://\S+|www\.\S+", " ", text)
    text = re.sub(r"\b[\w\.-]+@[\w\.-]+\.\w+\b", " ", text)
    text = re.sub(r"[^a-z0-9\s]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


@lru_cache(maxsize=1)
def load_ticket_model() -> tf.keras.Model:
    if not MODEL_PATH.exists():
        raise FileNotFoundError(f"Model file was not found: {MODEL_PATH}")
    return tf.keras.models.load_model(MODEL_PATH)


@lru_cache(maxsize=1)
def load_label_mapping() -> dict:
    if not LABEL_MAPPING_PATH.exists():
        raise FileNotFoundError(f"Label mapping file was not found: {LABEL_MAPPING_PATH}")

    with LABEL_MAPPING_PATH.open("r", encoding="utf-8") as file:
        return json.load(file)


def resolve_id_to_label(label_mapping: dict) -> dict[str, str]:
    if "id_to_label" in label_mapping and isinstance(label_mapping["id_to_label"], dict):
        return {str(key): str(value) for key, value in label_mapping["id_to_label"].items()}

    if "label_to_id" in label_mapping and isinstance(label_mapping["label_to_id"], dict):
        return {str(value): str(key) for key, value in label_mapping["label_to_id"].items()}

    if all(str(key).isdigit() for key in label_mapping):
        return {str(key): str(value) for key, value in label_mapping.items()}

    raise ValueError("Unsupported label_mapping.json structure.")


def preload_resources() -> None:
    load_ticket_model()
    resolve_id_to_label(load_label_mapping())


def predict_probabilities(text: str) -> list[dict[str, Any]]:
    if not str(text).strip():
        raise PredictorInputError("Input text must not be empty or whitespace only.")

    cleaned_text = clean_text(text)
    if not cleaned_text:
        raise PredictorInputError("Input text is empty after cleaning.")

    model = load_ticket_model()
    label_mapping = load_label_mapping()
    id_to_label = resolve_id_to_label(label_mapping)

    input_tensor = tf.constant([cleaned_text], dtype=tf.string)
    probabilities = model.predict(input_tensor, verbose=0)[0]

    predictions = [
        {
            "category": id_to_label.get(str(index)),
            "probability": float(probability),
        }
        for index, probability in enumerate(probabilities)
    ]

    if any(item["category"] is None for item in predictions):
        raise ValueError("Label mapping does not contain categories for all model output indexes.")

    return sorted(predictions, key=lambda item: item["probability"], reverse=True)


def predict_ticket(text: str, top_k: int = 3) -> dict[str, Any]:
    if top_k < 1:
        raise ValueError("top_k must be greater than zero.")

    predictions = predict_probabilities(text)
    top_predictions = predictions[:top_k]
    best_prediction = top_predictions[0]

    return {
        "predictedCategory": best_prediction["category"],
        "confidence": best_prediction["probability"],
        "topPredictions": top_predictions,
    }


if __name__ == "__main__":
    sample_text = "I cannot enter my profile after password reset."
    print(json.dumps(predict_ticket(sample_text), ensure_ascii=False, indent=2))
