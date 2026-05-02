import { AvatarDropdown, AvatarName, Footer, Question, SelectLang } from '@/components';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import React, { useEffect } from 'react';
import defaultSettings from '../config/defaultSettings';
import { getLoginUserUsingGet } from '@/services/xiaohang-backend/userController';
import { requestConfig } from '@/requestConfig';
import { LinkedinOutlined, GlobalOutlined, MailOutlined } from '@ant-design/icons';
import { message } from 'antd';

const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

/**
 * OAuth payload decoded from the __oauth_data URL parameter in getInitialState.
 * Cleared from localStorage immediately after getInitialState processes it.
 */
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
 * @see https://umijs.org/zh-CN/plugins/plugin-initial-state
 *
 * OAuth flow:
 * 1. User clicks "Sign in with GitHub" on Login page
 * 2. GitHub redirects to backend /api/oauth/github/callback
 * 3. Backend creates/finds user, generates JWT, redirects to
 *    /user/login?__oauth_done=1&__oauth_data=<base64-json>
 * 4. This getInitialState detects __oauth_done, decodes the payload,
 *    sets initialState.loginUser, and IMMEDIATELY redirects to the
 *    final destination (/profile for new users, / for returning users).
 *    window.location.href prevents the Login component from ever rendering.
 *
 * Session verification (getLoginUserUsingGet) is a SEPARATE concern — it
 * runs after OAuth processing to populate initialState for the ProLayout
 * avatar/name. If it fails (user not in session), loginUser stays as
 * set by the OAuth data above.
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

  // ── OAuth callback: decode payload and redirect BEFORE any component renders ─────
  if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);

    if (params.get('__oauth_done') === '1' && params.get('__oauth_data')) {
      try {
        const json = atob(params.get('__oauth_data')!);
        const data: OAuthPayload = JSON.parse(json);
        console.log('[OAuth] Decoded payload, isNew:', data.isNew, 'userName:', data.userName);

        // Write token to localStorage so request interceptor sends it with every API call
        if (data.token) {
          localStorage.setItem('oauth_token', data.token);
        }

        // Build the final destination BEFORE setting initialState.
        // We MUST redirect with window.location.href so the browser navigates
        // away BEFORE the Login/ProLayout component renders (no flash).
        const target = data.isNew ? '/profile' : '/';
        console.log('[OAuth] Redirecting to:', target);
        window.location.href = target;
        // Return a placeholder — the browser is navigating away so this return
        // value is never used. We intentionally set loginUser so that if the
        // navigation is somehow blocked, the user ends up on the right page.
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
        console.error('[OAuth] Failed to decode callback data:', e);
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
    const params = new URLSearchParams(window.location.search);
    const oauthError = params.get('__oauth_error');
    if (oauthError) {
      const cleanUrl = window.location.href
        .replace(/([?&])__oauth_error=1/, '$1')
        .replace(/([?&])error=[^&]*/, '$1')
        .replace(/[?&]$/, '');
      window.history.replaceState(null, '', cleanUrl);
      message.error(oauthError || 'GitHub login failed.');
    }
  }, []);

  return <>{children}</>;
};

// ProLayout 配置
export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
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

      // Handle OAuth callback URLs with numeric state suffix (e.g. /user/login_123456789...)
      // Redirect to /user/login so UmiJS can match the route.
      const oauthStateMatch = pathname.match(/^\/user\/(?:login|profile)_\d+$/);
      if (oauthStateMatch) {
        const params = new URLSearchParams(window.location.search);
        const target = pathname.replace(/^(.+?)_\d+$/, '$1');
        window.location.href = target + (params.toString() ? '?' + params.toString() : '');
        return;
      }

      // Auth guard: redirect unauthenticated users away from protected pages.
      // Note: OAuth users have initialState.loginUser set by getInitialState above,
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
    childrenRender: (children) => (
      <OAuthBridge>{children}</OAuthBridge>
    ),
    ...initialState?.settings,
  };
};

export const request = {
  ...requestConfig,
};
