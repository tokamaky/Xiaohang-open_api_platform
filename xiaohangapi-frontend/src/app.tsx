import { AvatarDropdown, AvatarName, Footer, Question, SelectLang } from '@/components';
import { LinkOutlined } from '@ant-design/icons';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history, Link } from '@umijs/max';
import React, { useEffect } from 'react';
import defaultSettings from '../config/defaultSettings';
import { getLoginUserUsingGet } from '@/services/xiaohang-backend/userController';
import { requestConfig } from '@/requestConfig';
import { LinkedinOutlined, GlobalOutlined, MailOutlined } from '@ant-design/icons';
import { message } from 'antd';

const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

const OAUTH_TOKEN_KEY = 'oauth_token';
const OAUTH_PENDING_KEY = 'oauth_pending';
const OAUTH_USER_KEY = 'oauth_user';
const OAUTH_TARGET_KEY = 'oauth_target_path';

// Module-level state shared between getInitialState and onPageChange.
// localStorage is the source of truth for surviving full page reloads.
let pendingOAuthData: Record<string, unknown> | null = null;

/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<InitialState> {
  const { location } = history;
  console.log('[OAuth] getInitialState running, location.search:', location.search);

  const fetchUserInfo = async () => {
    try {
      const res = await getLoginUserUsingGet();
      return res.data;
    } catch (error) {
      history.push(loginPath);
    }
    return undefined;
  };

  // --- Client-side OAuth handling ---
  if (typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);
    const oauthDone = params.get('__oauth_done');
    const encodedData = params.get('__oauth_data');

    if (oauthDone === '1' && encodedData) {
      try {
        const json = atob(encodedData);
        const data = JSON.parse(json);
        pendingOAuthData = data;

        if (data.token) {
          localStorage.setItem(OAUTH_TOKEN_KEY, data.token as string);
        }
        localStorage.setItem(OAUTH_PENDING_KEY, '1');
        localStorage.setItem(OAUTH_USER_KEY, JSON.stringify(data));

        const basePath = window.location.pathname.replace(/^(.+?)_\d+$/, '$1') || '/';
        localStorage.setItem(OAUTH_TARGET_KEY, basePath);
        console.log('[OAuth] Stored OAuth data, target:', basePath);
      } catch (e) {
        console.error('[OAuth] Failed to decode callback data:', e);
      }
    }
  }

  // --- Normal flow ---
  if (location.pathname !== loginPath) {
    try {
      const loginUser = await getLoginUserUsingGet();
      return {
        fetchUserInfo,
        loginUser: loginUser.data,
        settings: defaultSettings as Partial<LayoutSettings>,
      };
    } catch {
      history.push(loginPath);
    }
  }

  return {
    fetchUserInfo,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

// ── Client-side OAuth error handler ─────────────────────────────────────────────
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

// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({ initialState, setInitialState }) => {
  return {
    actionsRender: () => [<Question key="doc" />, <SelectLang key="SelectLang" />],
    avatarProps: {
      src: initialState?.loginUser?.userAvatar,
      title: <AvatarName />,
      render: (_, avatarChildren) => {
        return <AvatarDropdown>{avatarChildren}</AvatarDropdown>;
      },
    },
    waterMarkProps: {
      content: initialState?.loginUser?.userName,
    },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { pathname } = history.location;
      if (!pathname) return;

      if (typeof window === 'undefined') return;

      // 1. If user lands on /user/login_xxx (GitHub OAuth callback URL),
      // redirect to /user/login so the route matches.
      const oauthStateMatch = pathname.match(/^\/user\/login_(\d+)$/);
      if (oauthStateMatch) {
        const params = new URLSearchParams(window.location.search);
        window.location.href = '/user/login?' + params.toString();
        return;
      }

      // 2. If OAuth data is pending, redirect to target page.
      // This handles the case where we just redirected from /user/login_xxx
      // to /user/login and need to move to the actual destination (/ or /profile).
      const targetPath = localStorage.getItem(OAUTH_TARGET_KEY);
      if (targetPath && pathname === loginPath) {
        localStorage.removeItem(OAUTH_TARGET_KEY);
        window.location.href = targetPath;
        return;
      }

      // 3. Normal auth guard.
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
        <a key="email" href="mailto:jxh186045@gmail.com">
          <MailOutlined />
          <span>Email Me</span>
        </a>,
      ]
      : [],

    menuHeaderRender: undefined,
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    // 增加一个 loading 的状态
    childrenRender: (children) => {
      return (
        <OAuthBridge>
          {children}
        </OAuthBridge>
      );
    },
    ...initialState?.settings,
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request = {
  ...requestConfig,
};
