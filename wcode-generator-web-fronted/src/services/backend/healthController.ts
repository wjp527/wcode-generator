// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** userRegister GET /api/health */
export async function userRegisterUsingGet(options?: { [key: string]: any }) {
  return request<string>('/api/health', {
    method: 'GET',
    ...(options || {}),
  });
}
