from typing import Literal

from pydantic import BaseModel, Field


class AlertAnalysisRequest(BaseModel):
    title: str = Field(min_length=3, max_length=200)
    description: str = Field(min_length=3, max_length=5000)
    source: str = Field(min_length=1, max_length=100)
    observed_severity: Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"] | None = None


class AlertAnalysisResponse(BaseModel):
    summary_vi: str
    recommended_severity: Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    confidence: float = Field(ge=0, le=1)
    remediation_steps: list[str]
    requires_human_review: bool = True
    provider: str = "mock"
