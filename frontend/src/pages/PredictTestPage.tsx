import { useState, type FormEvent } from 'react';
import { predictTicketText } from '../api/predictApi';
import { ConfidenceBadge } from '../components/ConfidenceBadge';
import { ErrorMessage } from '../components/ErrorMessage';
import type { PredictResponse } from '../types/api';

export function PredictTestPage() {
  const [text, setText] = useState('');
  const [result, setResult] = useState<PredictResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!text.trim()) {
      setError(new Error('Text is required.'));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await predictTicketText({ text: text.trim() });
      setResult(response);
    } catch (requestError) {
      setError(requestError);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-stack">
      <section className="page-header">
        <div>
          <span className="section-kicker">Prediction sandbox</span>
          <h1>Direct AI prediction test</h1>
          <p>This page calls `POST /api/predict`. Unlike ticket creation, this endpoint does not use the ticket fallback flow.</p>
        </div>
      </section>

      <div className="content-grid content-grid--wide">
        <section className="card">
          <form className="form-grid" onSubmit={handleSubmit}>
            <div className="form-field">
              <label htmlFor="text">Support text</label>
              <textarea
                id="text"
                name="text"
                value={text}
                onChange={(event) => setText(event.target.value)}
                rows={10}
                disabled={loading}
              />
            </div>

            <div className="form-actions">
              <button type="submit" className="button button--primary" disabled={loading}>
                {loading ? 'Predicting...' : 'Run prediction'}
              </button>
            </div>
          </form>
        </section>

        <section className="card">
          <h2>Prediction result</h2>
          {error ? <ErrorMessage error={error} /> : null}

          {!error && !result && (
            <div className="placeholder-box">
              <p>Submit some text to inspect the raw prediction response.</p>
            </div>
          )}

          {result && (
            <div className="result-stack">
              <div className="result-grid">
                <div>
                  <span className="data-label">Predicted category</span>
                  <strong>{result.predictedCategory}</strong>
                </div>
                <div>
                  <span className="data-label">Confidence</span>
                  <ConfidenceBadge confidence={result.confidence} />
                </div>
              </div>

              <div>
                <h3>Top predictions</h3>
                {result.topPredictions.length > 0 ? (
                  <ul className="prediction-list">
                    {result.topPredictions.map((prediction, index) => (
                      <li key={`${prediction.category}-${index}`}>
                        <span>
                          #{index + 1} {prediction.category}
                        </span>
                        <strong>{(prediction.probability * 100).toFixed(1)}%</strong>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="muted-text">The backend returned no secondary predictions.</p>
                )}
              </div>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
