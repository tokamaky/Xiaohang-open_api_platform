// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** listTopInvokeInterfaceInfo GET /api/analysis/top/interface/invoke */
export async function listTopInvokeInterfaceInfoUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListInterfaceInfoVO_>('/api/analysis/top/interface/invoke', {
    method: 'GET',
    ...(options || {}),
  });
}

/** listAllInvokeInterfaceInfo GET /api/analysis/top/interface/invoke/all */
export async function listAllInvokeInterfaceInfoUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListInterfaceInfoVO_>('/api/analysis/top/interface/invoke/all', {
    method: 'GET',
    ...(options || {}),
  });
}

/** listCurrentUserInvokeInterfaceInfo GET /api/analysis/user/interface/invoke */
export async function listCurrentUserInvokeInterfaceInfoUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListInterfaceInfoVO_>('/api/analysis/user/interface/invoke', {
    method: 'GET',
    ...(options || {}),
  });
}

/** listUserInvokeInterfaceInfo GET /api/analysis/admin/user/{userId}/interface/invoke */
export async function listUserInvokeInterfaceInfoUsingGet(userId: number, options?: { [key: string]: any }) {
  return request<API.BaseResponseListInterfaceInfoVO_>(`/api/analysis/admin/user/${userId}/interface/invoke`, {
    method: 'GET',
    ...(options || {}),
  });
}

/** listAllUsers GET /api/analysis/admin/user/list */
export async function listAllUsersUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListUser_>('/api/analysis/admin/user/list', {
    method: 'GET',
    ...(options || {}),
  });
}
