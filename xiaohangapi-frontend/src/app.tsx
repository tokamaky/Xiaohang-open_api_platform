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

/**
 * OAuth flow:
 * 1. User clicks "Sign in with GitHub"
 * 2. GitHub redirects to backend /api/oauth/github/callback
 * 3. Backend creates/finds user, generates JWT, redirects to
 *    /user/login#__oauth_done=1&__oauth_data=<base64-json>
 *    NOTE: uses hash (#) not query (?) so Railway always serves index.html
 * 4. This getInitialState detects __oauth_done in window.location.hash,
 *    decodes the payload, sets initialState.loginUser, and IMMEDIATELY
 *    redirects to the final destination (/profile for new users, / for returning).
 *
 * Using hash fragments (#) instead of query params (?) solves the issue where
 * Railway returns 404 for /user/login_xxx, causing the 404.html JS to miss the
 * OAuth parameters. Hash fragments are never sent to the server, so Railway
 * always serves index.html and the frontend reads the OAuth data client-side.
 */
export async function getInitialState(): Promise<InitialState> {
  const fetchUserInfo = async () => {
    try {
      const res = await getLoginUserUsingGet();
      return res.data;
    } catch {
      history.push(loginPath);
    }
    return undefined;
  };

  // ── OAuth callback: read from hash, decode, redirect ─────────────────────────────
  // window.location.hash is never sent to the server, so Railway always serves
  // index.html for any callback URL — no 404 issues.
  if (typeof window !== 'undefined') {
    const hash = window.location.hash;

    if (hash && hash.includes('__oauth_done=1')) {
      try {
        const hashParams = new URLSearchParams(hash.substring(1)); // strip leading #
        const encodedData = hashParams.get('__oauth_data');
        if (!encodedData) throw new Error('Missing __oauth_data in hash');

        const json = atob(encodedData);
        const data: OAuthPayload = JSON.parse(json);
        console.log('[OAuth] Decoded from hash, isNew:', data.isNew, 'userName:', data.userName);

        // Write token so request interceptor sends it with every API call
        if (data.token) {
          localStorage.setItem('oauth_token', data.token);
        }

        const target = data.isNew ? '/profile' : '/';
        console.log('[OAuth] Redirecting to:', target);

        // Immediately redirect. Use window.location.href so the browser navigates
        // away before any component renders — prevents the Login page flash.
        window.location.href = target;

        // Return the user so initialState.loginUser is set even if navigation
        // is somehow blocked. This makes the auth guard happy on the next page.
        return {
          fetchUserInfo,
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
  }

  // ── Normal flow: verify session ────────────────────────────────────────────────
  try {
    const loginUser = await getLoginUserUsingGet();
    return {
      fetchUserInfo,
      loginUser: loginUser.data,
      settings: defaultSettings as Partial<LayoutSettings>,
    };
  } catch {
    // Not logged in
  }

  return {
    fetchUserInfo,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

// ── OAuth error handler: shows error toast on any page ──────────────────────────
const OAuthBridge: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  useEffect(() => {
    const hash = window.location.hash;
    if (hash && hash.includes('__oauth_error=1')) {
      const hashParams = new URLSearchParams(hash.substring(1));
      const errorMsg = hashParams.get('error') || 'GitHub login failed.';
      const cleanUrl = window.location.pathname; // strip hash
      window.history.replaceState(null, '', cleanUrl);
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
      // OAuth users have initialState.loginUser set by getInitialState above,
      // so they are NOT redirected here.
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
