import { useCallback, useEffect, useState } from 'react'
import { api, ApiError, type Session } from '../../lib/api'
import { AssetForm } from './AssetForm'
import { ASSET_STATUSES, ASSET_TYPES, CRITICALITIES, CRITICALITY_LABELS, STATUS_LABELS, TYPE_LABELS, type Asset, type AssetPage, type AssetQuery, type AssetRequest } from './types'

const EMPTY_PAGE: AssetPage = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 }
type Panel = { mode: 'create' } | { mode: 'detail' | 'edit'; asset: Asset } | null
function formatDate(value: string) { return new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) }

export function AssetWorkspace({ session, onSignOut }: { session: Session; onSignOut: () => void }) {
  const [query, setQuery] = useState<AssetQuery>({ page: 0, size: 10 })
  const [searchDraft, setSearchDraft] = useState('')
  const [data, setData] = useState(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [panel, setPanel] = useState<Panel>(null)
  const [notice, setNotice] = useState('')
  const canWrite = session.user.roles.some((role) => role === 'IT_MANAGER' || role === 'ANALYST')
  const canArchive = session.user.roles.includes('IT_MANAGER')

  const loadAssets = useCallback(async (signal?: AbortSignal) => {
    setLoading(true); setError('')
    try { setData(await api.assets(query, signal)) }
    catch (reason) {
      if (reason instanceof DOMException && reason.name === 'AbortError') return
      if (reason instanceof ApiError && reason.status === 401) { onSignOut(); return }
      setError(reason instanceof Error ? reason.message : 'Không thể tải danh sách tài sản.')
    } finally { if (!signal?.aborted) setLoading(false) }
  }, [query, onSignOut])

  useEffect(() => { const controller = new AbortController(); void loadAssets(controller.signal); return () => controller.abort() }, [loadAssets])
  useEffect(() => { if (!notice) return; const timeout = window.setTimeout(() => setNotice(''), 3500); return () => window.clearTimeout(timeout) }, [notice])
  function updateFilter(key: keyof AssetQuery, value: string | number) { setQuery((current) => ({ ...current, [key]: value, page: 0 })) }
  async function saveAsset(value: AssetRequest) { if (panel?.mode === 'edit') await api.updateAsset(panel.asset.id, value); else await api.createAsset(value); setPanel(null); setNotice(panel?.mode === 'edit' ? 'Đã cập nhật tài sản.' : 'Đã tạo tài sản mới.'); await loadAssets() }
  async function archive(asset: Asset) { if (!window.confirm(`Lưu trữ “${asset.name}”? Tài sản sẽ không thể chỉnh sửa sau thao tác này.`)) return; try { await api.archiveAsset(asset.id); setPanel(null); setNotice('Đã lưu trữ tài sản.'); await loadAssets() } catch (reason) { setError(reason instanceof Error ? reason.message : 'Không thể lưu trữ tài sản.') } }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#top"><span className="brand-mark">D</span><span>DefenSME</span></a>
        <nav aria-label="Điều hướng chính"><span className="nav-label">VẬN HÀNH</span><a className="nav-item active" href="#assets"><span className="nav-icon">▦</span>Tài sản</a><span className="nav-item disabled"><span className="nav-icon">◇</span>Cảnh báo <small>Sắp có</small></span><span className="nav-item disabled"><span className="nav-icon">◎</span>Sự cố <small>Sắp có</small></span><span className="nav-label">QUẢN TRỊ</span><span className="nav-item disabled"><span className="nav-icon">⌁</span>Nhật ký</span></nav>
        <div className="sidebar-foot"><div className="avatar">{session.user.displayName.slice(0, 1).toUpperCase()}</div><div><strong>{session.user.displayName}</strong><span>{session.user.roles.join(', ')}</span></div><button className="signout" onClick={onSignOut} aria-label="Đăng xuất">↗</button></div>
      </aside>

      <main className="workspace" id="assets">
        <header className="topbar"><div><p className="breadcrumb">Không gian làm việc / Tài sản</p><h1>Kiểm kê tài sản</h1><p className="muted">Theo dõi các hệ thống, thiết bị và dịch vụ cần được bảo vệ.</p></div>{canWrite && <button className="button primary" onClick={() => setPanel({ mode: 'create' })}><span>＋</span> Thêm tài sản</button>}</header>
        <section className="summary-row" aria-label="Tổng quan tài sản"><div className="summary-card"><span>TỔNG TÀI SẢN</span><strong>{data.totalElements}</strong><small>theo bộ lọc hiện tại</small></div><div className="summary-card critical"><span>ƯU TIÊN CAO</span><strong>{data.content.filter((item) => item.criticality === 'CRITICAL' || item.criticality === 'HIGH').length}</strong><small>trên trang này</small></div><div className="summary-card"><span>TRẠNG THÁI</span><strong className="textual">{query.status ? STATUS_LABELS[query.status as keyof typeof STATUS_LABELS] : 'Tất cả'}</strong><small>đang hiển thị</small></div></section>
        <section className="data-panel">
          <form className="toolbar" onSubmit={(e) => { e.preventDefault(); updateFilter('search', searchDraft.trim()) }}><label className="search"><span>⌕</span><input value={searchDraft} onChange={(e) => setSearchDraft(e.target.value)} placeholder="Tìm theo tên hoặc định danh…" aria-label="Tìm tài sản" /></label><select aria-label="Lọc loại tài sản" value={query.type ?? ''} onChange={(e) => updateFilter('type', e.target.value)}><option value="">Tất cả loại</option>{ASSET_TYPES.map((value) => <option key={value} value={value}>{TYPE_LABELS[value]}</option>)}</select><select aria-label="Lọc mức quan trọng" value={query.criticality ?? ''} onChange={(e) => updateFilter('criticality', e.target.value)}><option value="">Mọi mức độ</option>{CRITICALITIES.map((value) => <option key={value} value={value}>{CRITICALITY_LABELS[value]}</option>)}</select><select aria-label="Lọc trạng thái" value={query.status ?? ''} onChange={(e) => updateFilter('status', e.target.value)}><option value="">Mọi trạng thái</option>{ASSET_STATUSES.map((value) => <option key={value} value={value}>{STATUS_LABELS[value]}</option>)}</select><button className="button secondary">Tìm kiếm</button></form>
          {error && <div className="alert error table-alert" role="alert">{error}<button onClick={() => void loadAssets()}>Thử lại</button></div>}
          <div className="table-wrap"><table><thead><tr><th>Tài sản</th><th>Loại</th><th>Mức độ</th><th>Trạng thái</th><th>Cập nhật</th><th><span className="sr-only">Thao tác</span></th></tr></thead><tbody>{loading ? Array.from({ length: 5 }).map((_, index) => <tr className="skeleton-row" key={index}><td colSpan={6}><span /></td></tr>) : data.content.map((asset) => <tr key={asset.id} onClick={() => setPanel({ mode: 'detail', asset })} tabIndex={0} onKeyDown={(e) => { if (e.key === 'Enter') setPanel({ mode: 'detail', asset }) }}><td><div className="asset-name"><span className="asset-glyph">{asset.name.slice(0, 2).toUpperCase()}</span><div><strong>{asset.name}</strong><code>{asset.identifier}</code></div></div></td><td>{TYPE_LABELS[asset.type]}</td><td><span className={`pill severity-${asset.criticality.toLowerCase()}`}>{CRITICALITY_LABELS[asset.criticality]}</span></td><td><span className={`status-dot ${asset.status.toLowerCase()}`}>{STATUS_LABELS[asset.status]}</span></td><td>{formatDate(asset.updatedAt)}</td><td><button className="icon-button" aria-label={`Xem ${asset.name}`} onClick={(e) => { e.stopPropagation(); setPanel({ mode: 'detail', asset }) }}>→</button></td></tr>)}</tbody></table></div>
          {!loading && !error && data.content.length === 0 && <div className="empty-state"><div className="empty-icon">▦</div><h3>Chưa tìm thấy tài sản</h3><p>Thay đổi bộ lọc hoặc tạo tài sản đầu tiên cho tổ chức.</p>{canWrite && <button className="button primary" onClick={() => setPanel({ mode: 'create' })}>Thêm tài sản</button>}</div>}
          <footer className="pagination"><span>Hiển thị {data.content.length} / {data.totalElements} tài sản</span><div><select aria-label="Số dòng mỗi trang" value={query.size} onChange={(e) => updateFilter('size', Number(e.target.value))}><option value={10}>10 / trang</option><option value={20}>20 / trang</option><option value={50}>50 / trang</option></select><button disabled={data.page <= 0} onClick={() => setQuery((q) => ({ ...q, page: q.page - 1 }))}>←</button><span>Trang {data.totalPages ? data.page + 1 : 0} / {data.totalPages}</span><button disabled={data.page + 1 >= data.totalPages} onClick={() => setQuery((q) => ({ ...q, page: q.page + 1 }))}>→</button></div></footer>
        </section>
      </main>

      {panel && <div className="overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setPanel(null) }}><section className="drawer" role="dialog" aria-modal="true" aria-labelledby="drawer-title"><header><div><p className="eyebrow">{panel.mode === 'create' ? 'TÀI SẢN MỚI' : panel.mode === 'edit' ? 'CHỈNH SỬA' : 'CHI TIẾT TÀI SẢN'}</p><h2 id="drawer-title">{panel.mode === 'create' ? 'Thêm tài sản' : panel.asset.name}</h2></div><button className="icon-button" onClick={() => setPanel(null)} aria-label="Đóng">×</button></header>{panel.mode === 'detail' ? <div className="detail-body"><div className="detail-hero"><span className="asset-glyph large">{panel.asset.name.slice(0, 2).toUpperCase()}</span><div><code>{panel.asset.identifier}</code><div className="detail-pills"><span className={`pill severity-${panel.asset.criticality.toLowerCase()}`}>{CRITICALITY_LABELS[panel.asset.criticality]}</span><span className={`status-dot ${panel.asset.status.toLowerCase()}`}>{STATUS_LABELS[panel.asset.status]}</span></div></div></div><dl><div><dt>Loại tài sản</dt><dd>{TYPE_LABELS[panel.asset.type]}</dd></div><div><dt>Người phụ trách</dt><dd>{panel.asset.ownerUserId || 'Chưa phân công'}</dd></div><div><dt>Ngày tạo</dt><dd>{formatDate(panel.asset.createdAt)}</dd></div><div><dt>Cập nhật cuối</dt><dd>{formatDate(panel.asset.updatedAt)}</dd></div></dl><div className="metadata"><h3>Metadata</h3><pre>{JSON.stringify(panel.asset.metadata, null, 2)}</pre></div>{panel.asset.status !== 'ARCHIVED' && <div className="dialog-actions">{canArchive && <button className="button danger" onClick={() => void archive(panel.asset)}>Lưu trữ</button>}{canWrite && <button className="button primary" onClick={() => setPanel({ mode: 'edit', asset: panel.asset })}>Chỉnh sửa</button>}</div>}</div> : <AssetForm asset={panel.mode === 'edit' ? panel.asset : undefined} onCancel={() => setPanel(null)} onSubmit={saveAsset} />}</section></div>}
      {notice && <div className="toast" role="status">✓ {notice}</div>}
    </div>
  )
}
