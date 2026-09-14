from fastapi import APIRouter, Depends

from app.core.config import Settings, get_settings
from app.schemas.analysis import AlertAnalysisRequest, AlertAnalysisResponse
from app.services.alert_analyzer import AlertAnalyzer

router = APIRouter()


@router.get("/health")
def health(settings: Settings = Depends(get_settings)) -> dict[str, str]:
    return {
        "service": "defensme-ai-service",
        "status": "UP",
        "provider": settings.ai_provider,
    }


@router.post("/api/v1/analyses/alerts", response_model=AlertAnalysisResponse)
def analyze_alert(request: AlertAnalysisRequest) -> AlertAnalysisResponse:
    return AlertAnalyzer().analyze(request)
