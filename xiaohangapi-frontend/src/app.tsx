import { AvatarDropdown, AvatarName, Footer, Question } from '@/components';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import React, { useEffect } from 'react';
import defaultSettings from '../config/defaultSettings';
import { getLoginUserUsingGet } from '@/services/xiaohang-backend/userController';
import { requestConfig } from '@/requestConfig';
import { LinkedinOutlined, GlobalOutlined } from '@ant-design/icons';
import { message } from 'antd';

const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

// ── OAuth user data shape ───────────────────────────────────────────────────────
interface OAuthPayload {
  token: string;
  userAccount: string;
  userName: string;
  userAvatar: string;
  userRole: string;
  githubId: string;
  id: number;
  isNew: boolean;
}

// ── Session storage keys ──────────────────────────────────────────────────────────
const SESSION_USER_KEY = 'oauth_session_user';

/**
 * WHY THIS APPROACH WORKS:
 *
 * Railway serverless cold-starts mean the session cookie from the OAuth callback
 * may not be persisted to Redis by the time the next serverless invocation handles
 * getLoginUserUsingGet(). This causes a 401 on first load.
 *
 * Instead of relying on session-based API verification, we:
 * 1. Decode the OAuth payload from the URL hash (contains ALL user fields)
 * 2. Store it in sessionStorage (survives full page reloads)
 * 3. Use it directly as initialState.loginUser (NO API call needed)
 * 4. On subsequent page loads, read from sessionStorage
 *
 * The backend session is still used for OTHER authenticated API calls
 * (interface info, etc.) via the session cookie — this only affects the
 * initial state hydration after GitHub OAuth.
 */
export async function getInitialState(): Promise<InitialState> {
  // ── 1. Read OAuth data from URL hash (first-time callback) ─────────────────────
  // Hash fragments are never sent to the server, so Railway always serves index.html.
  // This handles the OAuth redirect: /user/login#__oauth_done=1&__oauth_data=...
  if (typeof window !== 'undefined') {
    const hash = window.location.hash;

    if (hash && hash.includes('__oauth_done=1')) {
      try {
        const hashParams = new URLSearchParams(hash.substring(1));
        const encodedData = hashParams.get('__oauth_data');
        if (!encodedData) throw new Error('Missing __oauth_data in hash');

        const json = atob(encodedData);
        const data: OAuthPayload = JSON.parse(json);
        console.log('[OAuth] First load from hash, isNew:', data.isNew, 'userName:', data.userName);

        // Persist to sessionStorage so this survives full page reloads
        sessionStorage.setItem(SESSION_USER_KEY, JSON.stringify(data));

        // Write token for request interceptor to attach to subsequent API calls
        if (data.token) {
          localStorage.setItem('oauth_token', data.token);
        }

        // Clean URL: strip the hash (don't need it anymore)
        window.history.replaceState(null, '', window.location.pathname);

        const target = data.isNew ? '/profile' : '/';
        console.log('[OAuth] Redirecting to:', target);
        window.location.href = target;

        // Return user so initialState.loginUser is set even if navigation is blocked
        return {
          fetchUserInfo: async () => undefined,
          loginUser: {
            id: data.id,
            userAccount: data.userAccount,
            userName: data.userName,
            userAvatar: data.userAvatar,
            userRole: data.userRole,
            githubId: data.githubId,
          } as InitialState['loginUser'],
          settings: defaultSettings as Partial<LayoutSettings>,
        };
      } catch (e) {
        console.error('[OAuth] Failed to decode hash callback data:', e);
        message.error('GitHub login failed. Please try again.');
      }
    }

    // ── 2. Restore from sessionStorage (subsequent page loads / second attempt) ───
    const storedUser = sessionStorage.getItem(SESSION_USER_KEY);
    if (storedUser) {
      try {
        const data: OAuthPayload = JSON.parse(storedUser);
        console.log('[OAuth] Restored from sessionStorage, userName:', data.userName);

        // Ensure token is in localStorage (sessionStorage doesn't persist across full reloads)
        if (data.token) {
          localStorage.setItem('oauth_token', data.token);
        }

        return {
          fetchUserInfo: async () => undefined,
          loginUser: {
            id: data.id,
            userAccount: data.userAccount,
            userName: data.userName,
            userAvatar: data.userAvatar,
            userRole: data.userRole,
            githubId: data.githubId,
          } as InitialState['loginUser'],
          settings: defaultSettings as Partial<LayoutSettings>,
        };
      } catch (e) {
        console.error('[OAuth] Failed to parse stored session user:', e);
        sessionStorage.removeItem(SESSION_USER_KEY);
      }
    }
  }

  // ── 3. Normal flow: session-based verification ─────────────────────────────────
  // For non-OAuth logins (e.g., cookie-based login), fall back to API call.
  try {
    const loginUser = await getLoginUserUsingGet();
    return {
      fetchUserInfo: async () => {
        try {
          return (await getLoginUserUsingGet()).data;
        } catch {
          history.push(loginPath);
        }
        return undefined;
      },
      loginUser: loginUser.data,
      settings: defaultSettings as Partial<LayoutSettings>,
    };
  } catch {
    // Not logged in
  }

  return {
    fetchUserInfo: async () => undefined,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

// ── OAuth error handler ─────────────────────────────────────────────────────────
const OAuthBridge: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  useEffect(() => {
    const hash = window.location.hash;
    if (hash && hash.includes('__oauth_error=1')) {
      const hashParams = new URLSearchParams(hash.substring(1));
      const errorMsg = hashParams.get('error') || 'GitHub login failed.';
      window.history.replaceState(null, '', window.location.pathname);
      message.error(decodeURIComponent(errorMsg));
    }
  }, []);

  return <>{children}</>;
};

// ProLayout 配置
export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    actionsRender: () => [<Question key="doc" />],
    avatarProps: {
      src: initialState?.loginUser?.userAvatar,
      title: <AvatarName />,
      render: (_, avatarChildren) => <AvatarDropdown>{avatarChildren}</AvatarDropdown>,
    },
    waterMarkProps: {
      content: initialState?.loginUser?.userName,
    },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { pathname } = history.location;
      if (!pathname || typeof window === 'undefined') return;

      // Auth guard: redirect unauthenticated users away from protected pages.
      if (!initialState?.loginUser && pathname !== loginPath) {
        history.push(loginPath);
      }
    },
    layoutBgImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
    ],
    links: isDev
      ? [
          <a key="linkedin" href="https://www.linkedin.com/in/xiaohang-ji" target="_blank" rel="noopener noreferrer">
            <LinkedinOutlined />
            <span>LinkedIn</span>
          </a>,
          <a key="personal-web" href="https://xiaohang-ji-profile.netlify.app/" target="_blank" rel="noopener noreferrer">
            <GlobalOutlined />
            <span>Profile</span>
          </a>,
        ]
      : [],
    menuHeaderRender: undefined,
    childrenRender: (children) => <OAuthBridge>{children}</OAuthBridge>,
    ...initialState?.settings,
  };
};

export const request = {
  ...requestConfig,
};
