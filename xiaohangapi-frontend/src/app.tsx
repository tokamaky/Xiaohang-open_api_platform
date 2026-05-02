import { AvatarDropdown, AvatarName, Footer, Question, SelectLang } from '@/components';
import { LinkOutlined } from '@ant-design/icons';
import type { Settings as LayoutSettings } from '@ant-design/pro-components';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history, Link } from '@umijs/max';
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

  const { location } = history;
  const urlParams = new URLSearchParams(location.search);
  console.log('[OAuth] urlParams __oauth_done:', urlParams.get('__oauth_done'));

  // --- Handle GitHub OAuth callback ---
  // Login data is encoded in __oauth_data URL param (set by backend callback).
  // This avoids relying on session which doesn't work across Railway serverless containers.
  if (urlParams.get('__oauth_done') === '1') {
    const encodedData = urlParams.get('__oauth_data');
    console.log('[OAuth] Callback detected, encodedData:', encodedData);
    if (encodedData) {
      try {
        const json = atob(encodedData);
        console.log('[OAuth] Decoded JSON:', json);
        const data = JSON.parse(json);
        console.log('[OAuth] Parsed data:', data);
        if (data.token) {
          localStorage.setItem(OAUTH_TOKEN_KEY, data.token);
          console.log('[OAuth] Token stored in localStorage');
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
        console.log('[OAuth] loginUser built:', loginUser);
        // Clean the URL of OAuth params while preserving the user state.
        const cleanUrl = location.pathname.replace(/^(.+?)_\d+$/, '$1') || '/';
        const urlWithParams = new URL(window.location.href);
        urlWithParams.searchParams.delete('__oauth_done');
        urlWithParams.searchParams.delete('__oauth_data');
        window.history.replaceState(null, '', urlWithParams.pathname + urlWithParams.search);
        console.log('[OAuth] URL cleaned, redirecting to:', cleanUrl);
        history.push(cleanUrl);
        return {
          fetchUserInfo,
          loginUser,
          settings: defaultSettings as Partial<LayoutSettings>,
        };
      } catch (e) {
        console.error('[OAuth] Failed to decode callback data:', e);
      }
    }
    message.error('GitHub login failed. Please try again.');
    history.push(loginPath);
  }

  if (urlParams.get('__oauth_error') === '1') {
    const errorMsg = urlParams.get('error') || 'GitHub OAuth failed';
    message.error(decodeURIComponent(errorMsg));
    history.push(loginPath);
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
