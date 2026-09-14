const capabilities = [
  ['Tài sản', 'Quản lý các hệ thống cần được bảo vệ'],
  ['Cảnh báo', 'Tiếp nhận và ưu tiên tín hiệu an ninh'],
  ['AI hỗ trợ', 'Giải thích rủi ro và đề xuất hướng xử lý'],
  ['Sự cố', 'Theo dõi trách nhiệm và tiến độ khắc phục'],
]

export function Dashboard() {
  return (
    <main className="shell">
      <header className="hero">
        <div className="brand-mark" aria-hidden="true">D</div>
        <div>
          <p className="eyebrow">CYBERSECURITY TEAM ON DEMAND</p>
          <h1>DefenSME</h1>
          <p className="subtitle">
            Phòng thủ an ninh mạng thông minh dành cho doanh nghiệp vừa và nhỏ.
          </p>
        </div>
      </header>

      <section aria-labelledby="status-heading">
        <div className="section-heading">
          <div>
            <p className="eyebrow">CODEBASE FOUNDATION</p>
            <h2 id="status-heading">Nền tảng đã sẵn sàng</h2>
          </div>
          <span className="status">Khởi tạo</span>
        </div>
        <p className="lead">
          Phạm vi chức năng chi tiết sẽ được triển khai sau khi PRD được kiểm chứng và phê duyệt.
        </p>
        <div className="grid">
          {capabilities.map(([title, description]) => (
            <article className="card" key={title}>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
