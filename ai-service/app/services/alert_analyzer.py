from app.schemas.analysis import AlertAnalysisRequest, AlertAnalysisResponse


class AlertAnalyzer:
    """Deterministic placeholder that keeps the API contract testable before AI integration."""

    def analyze(self, request: AlertAnalysisRequest) -> AlertAnalysisResponse:
        severity = request.observed_severity or "MEDIUM"
        return AlertAnalysisResponse(
            summary_vi=f"Cảnh báo '{request.title}' cần được nhân sự phụ trách kiểm tra.",
            recommended_severity=severity,
            confidence=0.5,
            remediation_steps=[
                "Xác minh nguồn và thời điểm phát sinh cảnh báo.",
                "Đối chiếu bằng chứng với nhật ký hệ thống liên quan.",
                "Chỉ thực hiện hành động khắc phục sau khi được phê duyệt.",
            ],
        )
