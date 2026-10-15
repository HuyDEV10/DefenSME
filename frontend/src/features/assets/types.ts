export const ASSET_TYPES = ['SERVER', 'WORKSTATION', 'NETWORK_DEVICE', 'APPLICATION', 'CLOUD_SERVICE', 'DATABASE', 'OTHER'] as const
export const CRITICALITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const
export const ASSET_STATUSES = ['ACTIVE', 'INACTIVE', 'ARCHIVED'] as const
export type AssetType = typeof ASSET_TYPES[number]
export type Criticality = typeof CRITICALITIES[number]
export type AssetStatus = typeof ASSET_STATUSES[number]
export interface Asset { id: string; organizationId: string; name: string; type: AssetType; identifier: string; criticality: Criticality; status: AssetStatus; ownerUserId: string | null; metadata: Record<string, unknown>; archivedAt: string | null; createdAt: string; updatedAt: string }
export interface AssetPage { content: Asset[]; page: number; size: number; totalElements: number; totalPages: number }
export interface AssetQuery { search?: string; type?: string; criticality?: string; status?: string; page: number; size: number }
export interface AssetRequest { name: string; type: AssetType; identifier: string; criticality: Criticality; status?: Exclude<AssetStatus, 'ARCHIVED'>; ownerUserId?: string | null; metadata: Record<string, unknown> }
export const TYPE_LABELS: Record<AssetType, string> = { SERVER: 'Máy chủ', WORKSTATION: 'Máy trạm', NETWORK_DEVICE: 'Thiết bị mạng', APPLICATION: 'Ứng dụng', CLOUD_SERVICE: 'Dịch vụ đám mây', DATABASE: 'Cơ sở dữ liệu', OTHER: 'Khác' }
export const CRITICALITY_LABELS: Record<Criticality, string> = { LOW: 'Thấp', MEDIUM: 'Trung bình', HIGH: 'Cao', CRITICAL: 'Nghiêm trọng' }
export const STATUS_LABELS: Record<AssetStatus, string> = { ACTIVE: 'Đang hoạt động', INACTIVE: 'Ngừng hoạt động', ARCHIVED: 'Đã lưu trữ' }
