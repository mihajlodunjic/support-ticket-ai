# CNN Text Prediction FastAPI Service

This folder contains the Python AI inference service for the helpdesk project.

It uses:

- Python 3.12
- TensorFlow 2.20.0
- the existing `models/cnn_text_model.keras`
- the existing `models/label_mapping.json`

## Create a virtual environment

From `predict_python_service/`:

```powershell
python -m venv .venv
.venv\Scripts\Activate.ps1
```

## Install dependencies

```powershell
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
```

## Run the service

From `predict_python_service/`:

```powershell
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

## Health check

```bash
curl http://127.0.0.1:8000/health
```

Example response:

```json
{
  "status": "OK",
  "modelLoaded": true,
  "labelsLoaded": true,
  "service": "cnn-text-prediction-service"
}
```

## Prediction request

```bash
curl -X POST http://127.0.0.1:8000/predict \
  -H "Content-Type: application/json" \
  -d '{
    "text": "I cannot access my account after password reset."
  }'
```

Example response:

```json
{
  "predictedCategory": "Access",
  "confidence": 0.87,
  "topPredictions": [
    {
      "category": "Access",
      "probability": 0.87
    },
    {
      "category": "Administrative rights",
      "probability": 0.08
    },
    {
      "category": "Hardware",
      "probability": 0.03
    }
  ]
}
```

## Notes

- The model and label mapping are loaded at application startup.
- `topPredictions` is limited to the top 3 classes.
- Empty, whitespace-only, or cleaned-empty text returns a controlled `400` response.
- If model loading or label mapping loading fails, the service reports `NOT_READY` on `/health` and returns a controlled error on `/predict`.
