from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "UP"


def test_alert_analysis_requires_human_review() -> None:
    response = client.post(
        "/api/v1/analyses/alerts",
        json={
            "title": "Đăng nhập bất thường",
            "description": "Tài khoản đăng nhập từ vị trí chưa từng ghi nhận.",
            "source": "identity-provider",
            "observed_severity": "HIGH",
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert payload["recommended_severity"] == "HIGH"
    assert payload["requires_human_review"] is True
