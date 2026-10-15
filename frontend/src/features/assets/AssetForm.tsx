import { useState } from 'react'
import { ASSET_TYPES, CRITICALITIES, CRITICALITY_LABELS, TYPE_LABELS, type Asset, type AssetRequest, type AssetStatus } from './types'

interface Props { asset?: Asset; onCancel: () => void; onSubmit: (value: AssetRequest) => Promise<void> }

export function AssetForm({ asset, onCancel, onSubmit }: Props) {
  const [name, setName] = useState(asset?.name ?? '')
  const [identifier, setIdentifier] = useState(asset?.identifier ?? '')
  const [type, setType] = useState(asset?.type ?? 'SERVER')
  const [criticality, setCriticality] = useState(asset?.criticality ?? 'MEDIUM')
  const [status, setStatus] = useState<Exclude<AssetStatus, 'ARCHIVED'>>(asset?.status === 'INACTIVE' ? 'INACTIVE' : 'ACTIVE')
  const [ownerUserId, setOwnerUserId] = useState(asset?.ownerUserId ?? '')
  const [metadata, setMetadata] = useState(JSON.stringify(asset?.metadata ?? {}, null, 2))
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function submit(event: React.FormEvent) {
    event.preventDefault(); setError('')
    if (!name.trim() || !identifier.trim()) { setError('Tên và định danh không được để trống.'); return }
    let parsed: Record<string, unknown>
    try {
      parsed = JSON.parse(metadata) as Record<string, unknown>
      if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') throw new Error()
      if (Object.keys(parsed).length > 25) { setError('Metadata chỉ được có tối đa 25 thuộc tính.'); return }
    } catch { setError('Metadata phải là một đối tượng JSON hợp lệ.'); return }
    setSaving(true)
    try { await onSubmit({ name: name.trim(), identifier: identifier.trim(), type, criticality, ...(asset ? { status } : {}), ownerUserId: ownerUserId.trim() || null, metadata: parsed }) }
    catch (reason) { setError(reason instanceof Error ? reason.message : 'Không thể lưu tài sản.') }
    finally { setSaving(false) }
  }

  return (
    <form className="asset-form" onSubmit={submit}>
      <div className="form-grid">
        <label className="wide">Tên tài sản<input autoFocus maxLength={150} required value={name} onChange={(e) => setName(e.target.value)} placeholder="Ví dụ: Máy chủ web chính" /></label>
        <label>Loại tài sản<select value={type} onChange={(e) => setType(e.target.value as typeof type)}>{ASSET_TYPES.map((value) => <option key={value} value={value}>{TYPE_LABELS[value]}</option>)}</select></label>
        <label>Định danh<input maxLength={200} required value={identifier} onChange={(e) => setIdentifier(e.target.value)} placeholder="srv-web-01" /></label>
        <label>Mức độ quan trọng<select value={criticality} onChange={(e) => setCriticality(e.target.value as typeof criticality)}>{CRITICALITIES.map((value) => <option key={value} value={value}>{CRITICALITY_LABELS[value]}</option>)}</select></label>
        {asset && <label>Trạng thái<select value={status} onChange={(e) => setStatus(e.target.value as typeof status)}><option value="ACTIVE">Đang hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select></label>}
        <label className="wide">Mã người phụ trách<input value={ownerUserId} onChange={(e) => setOwnerUserId(e.target.value)} placeholder="UUID, có thể để trống" /></label>
        <label className="wide">Metadata (JSON)<textarea rows={6} value={metadata} onChange={(e) => setMetadata(e.target.value)} spellCheck={false} /></label>
      </div>
      {error && <div className="alert error" role="alert">{error}</div>}
      <div className="dialog-actions"><button type="button" className="button ghost" onClick={onCancel}>Hủy</button><button className="button primary" disabled={saving}>{saving ? 'Đang lưu…' : asset ? 'Lưu thay đổi' : 'Tạo tài sản'}</button></div>
    </form>
  )
}
