// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** getCurrentUser GET /api/user/current */
export async function getCurrentUserUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseUser_>('/api/user/current', {
    method: 'GET',
    ...(options || {}),
  });
}

/** deleteUser POST /api/user/delete */
export async function deleteUserUsingPost(body: number, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogin POST /api/user/login */
export async function userLoginUsingPost(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseUser_>('/api/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** userLogout POST /api/user/logout */
export async function userLogoutUsingPost(options?: { [key: string]: any }) {
  return request<API.BaseResponseInt_>('/api/user/logout', {
    method: 'POST',
    ...(options || {}),
  });
}

/** userRegister POST /api/user/register */
export async function userRegisterUsingPost(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseLong_>('/api/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** searchUser GET /api/user/search */
export async function searchUserUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.searchUserUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseListUser_>('/api/user/search', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
