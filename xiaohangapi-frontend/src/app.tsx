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

  // --- Normal flow (SSR-safe) ---
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

// ── Client-side OAuth callback handler ────────────────────────────────────────
// This component runs ONLY in the browser (never during SSR), so window.location
// always has the real URL with __oauth_data params. It bridges the gap between the
// SSR render (where getInitialState can't see URL params) and the client render.
let oauthHydrated = false;
const OAuthBridge: React.FC<{ children: React.ReactNode; setInitialState: (state: InitialState | ((prev: InitialState) => InitialState)) => void }> = ({ children, setInitialState }) => {
  useEffect(() => {
    if (oauthHydrated) return;
    oauthHydrated = true;

    const params = new URLSearchParams(window.location.search);
    const oauthError = params.get('__oauth_error');
    if (oauthError) {
      const cleanUrl = window.location.href.replace(/([?&])__oauth_error=1/, '$1').replace(/([?&])error=[^&]*/, '$1').replace(/[?&]$/, '');
      window.history.replaceState(null, '', cleanUrl);
      message.error(oauthError || 'GitHub login failed.');
      return;
    }

    if (params.get('__oauth_done') !== '1') return;

    const encodedData = params.get('__oauth_data');
    if (!encodedData) return;

    try {
      const json = atob(encodedData);
      const data = JSON.parse(json);

      if (data.token) {
        localStorage.setItem(OAUTH_TOKEN_KEY, data.token);
      }

      localStorage.setItem('oauth_pending', '1');
      localStorage.setItem('oauth_user', JSON.stringify(data));

      const cleanUrl = window.location.href
        .replace(/([?&])__oauth_done=1/, '$1')
        .replace(/([?&])__oauth_data=[^&]*/, '$1')
        .replace(/[?&]$/, '');
      window.history.replaceState(null, '', cleanUrl);
    } catch (e) {
      console.error('[OAuth] Failed to decode callback data:', e);
    }
  }, [setInitialState]);

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
      const { location } = history;
      // 如果没有登录，重定向到 login
      if (!initialState?.loginUser && location.pathname !== loginPath) {
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
    childrenRender: (children, { setInitialState }) => {
      return (
        <OAuthBridge setInitialState={setInitialState}>
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
