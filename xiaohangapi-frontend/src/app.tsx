import { AvatarDropdown, AvatarName, Footer, Question, SelectLang } from '@/components';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history } from '@umijs/max';
import React from 'react';
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

  const fetchUserInfo = async () => {
    try {
      const res = await getLoginUserUsingGet();
      return res.data;
    } catch {
      return undefined;
    }
  };

  // --- Step 1: Try OAuth token from localStorage first (GitHub OAuth users) ---
  // This must come FIRST because the requestConfig interceptor injects the token
  // into all API calls, including getLoginUserUsingGet().
  const storedToken = localStorage.getItem(OAUTH_TOKEN_KEY);
  if (storedToken) {
    console.log('[Auth] Found OAuth token in localStorage, validating with API...');
    try {
      const res = await getLoginUserUsingGet();
      if (res.data) {
        console.log('[Auth] OAuth token valid, user:', res.data.userName);
        return {
          fetchUserInfo,
          loginUser: res.data,
          settings: defaultSettings as Partial<LayoutSettings>,
        };
      }
    } catch (e) {
      console.log('[Auth] OAuth token invalid or expired, clearing:', e);
      localStorage.removeItem(OAUTH_TOKEN_KEY);
    }
  }

  // --- Step 2: Check for GitHub OAuth callback in URL ---
  // Backend redirects here with ?__oauth_done=1&__oauth_data=<base64>
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('__oauth_done') === '1') {
    const encodedData = urlParams.get('__oauth_data');
    if (encodedData) {
      try {
        const json = atob(encodedData);
        const data = JSON.parse(json);
        if (data.token) {
          localStorage.setItem(OAUTH_TOKEN_KEY, data.token);
        }
        const loginUser = {
          id: data.id,
          token: data.token,
          userAccount: data.userAccount,
          userName: data.userName,
          userAvatar: data.userAvatar,
          userRole: data.userRole,
          githubId: data.githubId,
        };
        // Clean OAuth params from URL
        const url = new URL(window.location.href);
        url.searchParams.delete('__oauth_done');
        url.searchParams.delete('__oauth_data');
        window.history.replaceState(null, '', url.pathname + url.search);
        // Re-run getInitialState so the token in localStorage is picked up
        window.location.reload();
        return { fetchUserInfo, loginUser, settings: defaultSettings as Partial<LayoutSettings> };
      } catch (e) {
        console.error('[Auth] Failed to decode OAuth callback data:', e);
      }
    }
    message.error('GitHub login failed. Please try again.');
  }

  if (urlParams.get('__oauth_error') === '1') {
    const errorMsg = urlParams.get('error') || 'GitHub OAuth failed';
    message.error(decodeURIComponent(errorMsg));
    const url = new URL(window.location.href);
    url.searchParams.delete('__oauth_error');
    url.searchParams.delete('error');
    window.history.replaceState(null, '', url.pathname + url.search);
  }

  // --- Step 3: Try session-based login (regular users) ---
  // Skip this for the login page itself
  if (window.location.pathname !== loginPath) {
    try {
      const loginUser = await getLoginUserUsingGet();
      if (loginUser.data) {
        return {
          fetchUserInfo,
          loginUser: loginUser.data,
          settings: defaultSettings as Partial<LayoutSettings>,
        };
      }
    } catch {
      // Not logged in
    }
  }

  // --- Step 4: Not logged in ---
  return {
    fetchUserInfo,
    settings: defaultSettings as Partial<LayoutSettings>,
  };
}

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
    childrenRender: (children) => {
      // if (initialState?.loading) return <PageLoading />;
      return (
        <>
          {children}
          {/*<SettingDrawer*/}
          {/*    disableUrlParams*/}
          {/*    enableDarkTheme*/}
          {/*    settings={initialState?.settings}*/}
          {/*    onSettingChange={(settings) => {*/}
          {/*        setInitialState((preInitialState) => ({*/}
          {/*            ...preInitialState,*/}
          {/*            settings,*/}
          {/*        }));*/}
          {/*    }}*/}
          {/*/>*/}
        </>
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
