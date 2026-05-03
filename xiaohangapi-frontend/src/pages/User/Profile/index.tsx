import {
    updateSecretKeyUsingPost,
    updateUserUsingPost,
    updateMyUserUsingPost,
    userLoginUsingPost,
    getGithubAuthUrlUsingGet,
    bindGithubUsingPost,
    unbindGithubUsingPost,
    getLoginUserUsingGet,
    deleteMyAccountUsingPost,
    changePasswordUsingPost,
    setPasswordUsingPost,
} from '@/services/xiaohang-backend/userController';
import {useModel} from '@@/exports';
import {
    CheckCircleOutlined,
    CopyOutlined,
    FieldTimeOutlined,
    LoadingOutlined,
    LockOutlined,
    PlusOutlined,
    UnlockOutlined,
    UserOutlined,
    VerifiedOutlined,
    GithubOutlined,
    LinkOutlined,
    DisconnectOutlined,
    EditOutlined,
    SaveOutlined,
    DeleteOutlined,
} from '@ant-design/icons';
import {PageContainer, ProForm, ProFormInstance, ProFormText} from '@ant-design/pro-components';
import {Button, Card, Divider, Input, message, Modal, Typography, Upload, UploadFile, UploadProps, Tag, Popconfirm} from 'antd';
import {RcFile, UploadChangeParam} from 'antd/es/upload';
import React, {useEffect, useRef, useState} from 'react';
import './index.less';

const { Paragraph } = Typography;

const beforeUpload = (file: RcFile) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) message.error('Only JPG/PNG files are allowed!');
  const isLt2M = file.size / 1024 / 1024 < 5;
  if (!isLt2M) message.error('Max upload size is 5MB!');
  return isJpgOrPng && isLt2M;
};

const SESSION_USER_KEY = 'oauth_session_user';

const Profile: React.FC = () => {
  const [data, setData] = useState<API.UserVO>({});
  const [visible, setVisible] = useState<boolean>(false);
  const [flag, setFlag] = useState<boolean>(false);
  const [open, setOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>();
  const [editingUsername, setEditingUsername] = useState(false);
  const [usernameValue, setUsernameValue] = useState('');
  const [savingUsername, setSavingUsername] = useState(false);
  const { initialState, setInitialState } = useModel('@@initialState');
  const formRef = useRef<ProFormInstance<{ userPassword: string }>>();

  // Delete account modal
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);

  // Change password modal (for regular users)
  const [passwordModalOpen, setPasswordModalOpen] = useState(false);
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);

  // Set password modal (for GitHub OAuth users)
  const [setPwdModalOpen, setSetPwdModalOpen] = useState(false);
  const [setPwdValue, setSetPwdValue] = useState('');
  const [setPwdConfirmValue, setSetPwdConfirmValue] = useState('');
  const [setPwdLoading, setSetPwdLoading] = useState(false);

  // ── Restore user from sessionStorage on mount ──────────────────────────────
  // This is a fallback for when the profile page is reached directly (e.g. via
  // a bookmark or after a full browser restart) and the API call fails.
  useEffect(() => {
    if (!data?.id) {
      const stored = sessionStorage.getItem(SESSION_USER_KEY);
      if (stored) {
        try {
          const parsed = JSON.parse(stored);
          setInitialState((s: any) => ({ ...s, loginUser: parsed }));
          setData(parsed as API.UserVO);
          setImageUrl(parsed.userAvatar);
          setUsernameValue(parsed.userName || '');
        } catch {}
      }
    }
  }, [data?.id]);

  // ── GitHub OAuth callback ──────────────────────────────────────────────
  // Detect #__oauth_error=1 after GitHub redirects back.
  // Uses hash (#) instead of query params (?) so Railway always serves index.html.
  useEffect(() => {
    const hash = window.location.hash;
    if (!hash || (!hash.includes('__oauth_done=1') && !hash.includes('__oauth_error=1'))) return;

    // Check for error first - errors should be shown immediately
    if (hash.includes('__oauth_error=1')) {
      const hashParams = new URLSearchParams(hash.substring(1));
      const errorMsg = hashParams.get('error');
      // Clean URL first
      window.history.replaceState(null, '', window.location.pathname);

      // Show error message - use setTimeout to ensure message API is ready
      setTimeout(() => {
        if (errorMsg) {
          try {
            message.error(decodeURIComponent(errorMsg));
          } catch {
            message.error(errorMsg);
          }
        } else {
          message.error('Failed to link GitHub account. Please try again.');
        }
        getUserInfo();
      }, 100);
      return;
    }

    // Process success case
    if (hash.includes('__oauth_done=1')) {
      const hashParams = new URLSearchParams(hash.substring(1)); // strip leading #
      const encodedData = hashParams.get('__oauth_data');
      // Clean URL
      window.history.replaceState(null, '', window.location.pathname);

      if (encodedData) {
        try {
          const json = atob(encodedData);
          const oauthData = JSON.parse(json);
          if (oauthData.token) {
            localStorage.setItem('oauth_token', oauthData.token);
          }
          // Persist to sessionStorage so getUserInfo can use it as fallback
          sessionStorage.setItem(SESSION_USER_KEY, JSON.stringify(oauthData));

          setInitialState((s: any) => ({ ...s, loginUser: oauthData }));
          setData(oauthData as API.UserVO);
          setImageUrl(oauthData.userAvatar);
          setUsernameValue(oauthData.userName || '');

          // Only show "linked successfully" for link action
          if (oauthData.oauthAction === 'link') {
            message.success('GitHub account linked successfully!');
          }
        } catch (e) {
          console.error('[OAuth] Failed to decode profile callback data:', e);
          getUserInfo();
        }
      } else {
        getUserInfo();
      }
    }
  }, []);

  const getUserInfo = async () => {
    return getLoginUserUsingGet().then((res) => {
      if (res.data) {
        // Also persist to sessionStorage as backup
        sessionStorage.setItem(SESSION_USER_KEY, JSON.stringify(res.data));
        setInitialState((s: any) => ({ ...s, loginUser: res.data }));
        setData(res.data as API.UserVO);
        setImageUrl(res.data.userAvatar);
        setUsernameValue(res.data.userName || '');
      }
    }).catch(() => {
      // API failed (e.g. Railway serverless cold-start) — fall back to sessionStorage
      const stored = sessionStorage.getItem(SESSION_USER_KEY);
      if (stored) {
        try {
          const parsed = JSON.parse(stored);
          setInitialState((s: any) => ({ ...s, loginUser: parsed }));
          setData(parsed as API.UserVO);
          setImageUrl(parsed.userAvatar);
          setUsernameValue(parsed.userName || '');
        } catch {}
      }
    });
  };

  const saveUsername = async () => {
    if (!usernameValue.trim()) {
      message.error('Username cannot be empty');
      return;
    }
    setSavingUsername(true);
    try {
      const res = await updateMyUserUsingPost({ userName: usernameValue.trim() });
      if (res.code === 0) {
        message.success('Username updated!');
        setEditingUsername(false);
        await getUserInfo();
      }
    } finally {
      setSavingUsername(false);
    }
  };

  useEffect(() => {
    try {
      getUserInfo();
    } catch (e: any) {
      console.log(e);
    }
  }, []);

  const showSecretKey = async () => {
    let userPassword = formRef?.current?.getFieldValue('userPassword');
    const res = await userLoginUsingPost({
      userAccount: data?.userAccount,
      userPassword: userPassword,
    });
    if (res.code === 0) {
      setOpen(false);
      setVisible(true);
      formRef?.current?.resetFields();
    }
  };

  const updateUserAvatar = async (userAvatar: string) => {
    const id = initialState?.loginUser?.id;
    if (!id) return;
    const res = await updateUserUsingPost({ id, userAvatar });
    if (res.code !== 0) {
      message.error(`Failed to update avatar`);
    } else {
      getUserInfo();
    }
  };

  const handleChange: UploadProps['onChange'] = (info: UploadChangeParam<UploadFile>) => {
    if (info.file.status === 'uploading') { setLoading(true); return; }
    if (info.file.status === 'done' && info.file.response?.code === 0) {
      message.success(`Avatar updated`);
      const id = initialState?.loginUser?.id as number;
      const userAvatar = info.file.response.data.url;
      setLoading(false);
      setImageUrl(userAvatar);
      updateUserAvatar(userAvatar);
    }
  };

  const resetSecretKey = async () => {
    try {
      let userPassword = formRef?.current?.getFieldValue('userPassword');
      const res = await userLoginUsingPost({
        userAccount: data?.userAccount,
        userPassword: userPassword,
      });
      if (res.code === 0) {
        const res2 = await updateSecretKeyUsingPost({ id: data?.id });
        if (res2.data) {
          getUserInfo();
          message.success('Secret key reset successfully!');
          setOpen(false);
        }
      }
    } catch (e: any) {
      console.log(e);
    }
  };

  const handleChangePassword = async () => {
    if (!oldPassword) {
      message.error('Please enter your current password');
      return;
    }
    if (!newPassword) {
      message.error('Please enter a new password');
      return;
    }
    if (newPassword.length < 8) {
      message.error('New password must be at least 8 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      message.error('New passwords do not match');
      return;
    }
    setPasswordLoading(true);
    try {
      const res = await changePasswordUsingPost({
        userPassword: oldPassword,
        newPassword: newPassword,
      });
      if (res.code === 0) {
        message.success('Password changed successfully!');
        setPasswordModalOpen(false);
        setOldPassword('');
        setNewPassword('');
        setConfirmPassword('');
      }
    } catch (e: any) {
      message.error(e?.message || 'Failed to change password');
    } finally {
      setPasswordLoading(false);
    }
  };

  const handleSetPassword = async () => {
    if (!setPwdValue) {
      message.error('Please enter a password');
      return;
    }
    if (setPwdValue.length < 8) {
      message.error('Password must be at least 8 characters');
      return;
    }
    if (setPwdValue !== setPwdConfirmValue) {
      message.error('Passwords do not match');
      return;
    }
    setSetPwdLoading(true);
    try {
      const res = await setPasswordUsingPost({
        newPassword: setPwdValue,
      });
      if (res.code === 0) {
        message.success('Password set successfully!');
        setSetPwdModalOpen(false);
        setSetPwdValue('');
        setSetPwdConfirmValue('');
        // Refresh user info to update the UI
        await getUserInfo();
      }
    } catch (e: any) {
      message.error(e?.message || 'Failed to set password');
    } finally {
      setSetPwdLoading(false);
    }
  };

  // Determine if this is a GitHub user
  const isGithubUser = !!data?.githubId;

  const handleDeleteAccount = async () => {
    if (!isGithubUser && !deletePassword) {
      message.error('Please enter your password to confirm');
      return;
    }
    setDeleteLoading(true);
    try {
      const res = await deleteMyAccountUsingPost({
        userPassword: deletePassword || '',
      });
      if (res.code === 0) {
        message.success('Account deleted successfully');
        // Clear local storage
        localStorage.removeItem('oauth_token');
        sessionStorage.removeItem(SESSION_USER_KEY);
        // Redirect to home page
        setTimeout(() => {
          window.location.href = '/';
        }, 1000);
      }
    } catch (e: any) {
      message.error(e?.message || 'Failed to delete account');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <PageContainer>
      <div className="profile-page">
        {/* Left — User Info */}
        <Card className="profile-card profile-card-main" bordered={false}>
          <div className="profile-avatar-wrap">
            <Upload
              name="file"
              listType="picture-circle"
              showUploadList={false}
              action="http://124.70.63.241:8101/api/file/upload"
              beforeUpload={beforeUpload}
              onChange={handleChange}
              className="avatar-uploader"
            >
              {imageUrl ? (
                <img src={data?.userAvatar} alt="avatar" className="avatar-img" />
              ) : (
                <div className="avatar-placeholder">
                  {loading ? <LoadingOutlined /> : <PlusOutlined />}
                  <span>Upload</span>
                </div>
              )}
            </Upload>
            {data.userRole && (
              <span className="role-badge">
                <VerifiedOutlined /> {data.userRole}
              </span>
            )}
          </div>

          <Divider className="profile-divider" />

          <div className="profile-info-list">
            <div className="info-row">
              <span className="info-icon"><UserOutlined /></span>
              <span className="info-label">Username</span>
              <span className="info-value">
                {editingUsername ? (
                  <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Input
                      value={usernameValue}
                      onChange={(e) => setUsernameValue(e.target.value)}
                      onPressEnter={saveUsername}
                      style={{ width: 140 }}
                      size="small"
                      autoFocus
                    />
                    <Button
                      type="text"
                      size="small"
                      icon={<SaveOutlined />}
                      loading={savingUsername}
                      onClick={saveUsername}
                      style={{ color: '#00D4AA' }}
                    />
                    <Button
                      type="text"
                      size="small"
                      onClick={() => {
                        setEditingUsername(false);
                        setUsernameValue(data?.userName || '');
                      }}
                    >
                      Cancel
                    </Button>
                  </span>
                ) : (
                  <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span>{data?.userName || '—'}</span>
                    <Button
                      type="text"
                      size="small"
                      icon={<EditOutlined />}
                      onClick={() => setEditingUsername(true)}
                      style={{ color: '#00A3FF', padding: '0 4px' }}
                    />
                  </span>
                )}
              </span>
            </div>
            <div className="info-row">
              <span className="info-icon"><UserOutlined /></span>
              <span className="info-label">Account</span>
              <span className="info-value">{data?.userAccount || '—'}</span>
            </div>
            <div className="info-row">
              <span className="info-icon"><FieldTimeOutlined /></span>
              <span className="info-label">Registered</span>
              <span className="info-value">{data?.createTime ? new Date(data.createTime).toLocaleDateString() : '—'}</span>
            </div>

            {/* GitHub Binding */}
            <div className="info-row">
              <span className="info-icon"><GithubOutlined /></span>
              <span className="info-label">GitHub</span>
              <span className="info-value">
                {data?.githubId ? (
                  <Tag color="green" icon={<GithubOutlined />}>Linked</Tag>
                ) : (
                  <Tag color="default" icon={<GithubOutlined />}>Not linked</Tag>
                )}
              </span>
            </div>

            {/* GitHub Link / Unlink Button */}
            <div className="info-row" style={{ marginTop: 8 }}>
              <span className="info-icon" />
              <span className="info-value" style={{ paddingLeft: 0 }}>
                {data?.githubId ? (
                  <Button
                    size="small"
                    danger
                    icon={<DisconnectOutlined />}
                    onClick={async () => {
                      try {
                        const res = await unbindGithubUsingPost();
                        if (res.code === 0 && res.data) {
                          message.success('GitHub account unlinked');
                          const unlinkedUser = res.data;
                          // Update all state sources so UI reflects githubId=null immediately
                          setData(unlinkedUser as API.UserVO);
                          setImageUrl(unlinkedUser.userAvatar);
                          setUsernameValue(unlinkedUser.userName || '');
                          setInitialState((s: any) => ({
                            ...s,
                            loginUser: { ...s.loginUser, githubId: null, userAvatar: unlinkedUser.userAvatar, userName: unlinkedUser.userName },
                          }));
                          // Persist to sessionStorage so refresh doesn't restore old state
                          const stored = sessionStorage.getItem(SESSION_USER_KEY);
                          if (stored) {
                            const parsed = JSON.parse(stored);
                            parsed.githubId = null;
                            parsed.userAvatar = unlinkedUser.userAvatar;
                            parsed.userName = unlinkedUser.userName;
                            sessionStorage.setItem(SESSION_USER_KEY, JSON.stringify(parsed));
                          }
                        } else {
                          message.error(res.message || 'Failed to unlink GitHub');
                        }
                      } catch (e: any) {
                        message.error(e?.message || 'Failed to unlink GitHub');
                      }
                    }}
                  >
                    Unlink GitHub
                  </Button>
                ) : (
                  <Button
                    size="small"
                    icon={<LinkOutlined />}
                    onClick={async () => {
                      try {
                        // Add action=link to distinguish from login
                        // Pass current token in Authorization header for serverless session verification
                        const token = localStorage.getItem('oauth_token');
                        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
                        const redirectUrl = encodeURIComponent('https://xiaohang-openapiplatform-production.up.railway.app/user/profile?oauth_action=link');
                        const res = await (window as any).fetch(
                          `https://backend-production-796b.up.railway.app/api/oauth/github/url?redirectUrl=${redirectUrl}`,
                          { headers }
                        ).then((r: Response) => r.json());
                        if (res.data) {
                          window.location.href = res.data;
                        } else {
                          message.error(res.message || 'Failed to get GitHub auth URL');
                        }
                      } catch (e) {
                        message.error('Failed to connect to GitHub');
                      }
                    }}
                  >
                    Link GitHub
                  </Button>
                )}
              </span>
            </div>

            {/* Password and Account Actions */}
            <div className="info-row" style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
              <span className="info-icon" />
              <span className="info-value" style={{ paddingLeft: 0, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {data?.userAccount?.startsWith('github_') ? (
                  <Button
                    size="small"
                    icon={<LockOutlined />}
                    onClick={() => setSetPwdModalOpen(true)}
                  >
                    Set Password
                  </Button>
                ) : (
                  <Button
                    size="small"
                    icon={<LockOutlined />}
                    onClick={() => setPasswordModalOpen(true)}
                  >
                    Change Password
                  </Button>
                )}
                <Button
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => setDeleteModalOpen(true)}
                >
                  Delete Account
                </Button>
              </span>
            </div>
          </div>
        </Card>

        {/* Right — Secret Keys */}
        <Card className="profile-card profile-card-keys" bordered={false}>
          <div className="keys-header">
            <LockOutlined className="keys-icon" />
            <h3 className="keys-title">API Credentials</h3>
            <p className="keys-subtitle">Keep these secret. Do not share with anyone.</p>
          </div>

          <Divider className="profile-divider" />

          <div className="key-block">
            <div className="key-label-row">
              <span className="key-name">Access Key</span>
              {visible && (
                <Button
                  className="copy-btn"
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={() => {
                    navigator.clipboard.writeText(data?.accessKey || '');
                    message.success('Access Key copied!');
                  }}
                />
              )}
            </div>
            <Paragraph
              copyable={visible ? { text: data?.accessKey } : false}
              className={`key-value ${!visible ? 'key-masked' : ''}`}
            >
              {visible ? data?.accessKey : '••••••••••••••••••••••••'}
            </Paragraph>
          </div>

          <Divider className="profile-divider" />

          <div className="key-block">
            <div className="key-label-row">
              <span className="key-name">Secret Key</span>
              {visible && (
                <Button
                  className="copy-btn"
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={() => {
                    navigator.clipboard.writeText(data?.secretKey || '');
                    message.success('Secret Key copied!');
                  }}
                />
              )}
            </div>
            <Paragraph
              copyable={visible ? { text: data?.secretKey } : false}
              className={`key-value ${!visible ? 'key-masked' : ''}`}
            >
              {visible ? data?.secretKey : '••••••••••••••••••••••••'}
            </Paragraph>
          </div>

          <Divider className="profile-divider" />

          <div className="key-actions">
            {visible ? (
              <Button
                className="key-action-btn key-action-hide"
                icon={<LockOutlined />}
                onClick={() => setVisible(false)}
              >
                Hide Keys
              </Button>
            ) : (
              <Button
                className="key-action-btn"
                icon={<UnlockOutlined />}
                onClick={() => { setOpen(true); setFlag(true); }}
              >
                View Keys
              </Button>
            )}
            <Button
              className="key-action-btn key-action-reset"
              icon={<CheckCircleOutlined />}
              onClick={() => { setOpen(true); setFlag(false); }}
              danger
            >
              Reset Keys
            </Button>
          </div>
        </Card>
      </div>

      <Modal
        title={
          <span className="modal-title">
            <LockOutlined /> {flag ? 'View Secret Keys' : 'Reset Secret Keys'}
          </span>
        }
        open={open}
        onOk={flag ? showSecretKey : resetSecretKey}
        onCancel={() => setOpen(false)}
        okText={flag ? 'Confirm' : 'Reset'}
        okButtonProps={{ danger: !flag }}
        className="profile-modal"
      >
        <p className="modal-desc">
          {flag
            ? 'Enter your password to reveal your API credentials.'
            : 'Resetting will generate new API credentials. Your old keys will stop working immediately.'}
        </p>
        <ProForm<{ userPassword: string }>
          formRef={formRef}
          autoFocusFirstInput
          submitter={{
            resetButtonProps: { style: { display: 'none' } },
            submitButtonProps: { style: { display: 'none' } },
          }}
        >
          <ProFormText.Password
            name="userPassword"
            placeholder="Enter your password"
            fieldProps={{ size: 'large' }}
          />
        </ProForm>
      </Modal>

      {/* Change Password Modal */}
      <Modal
        title={
          <span className="modal-title">
            <LockOutlined /> Change Password
          </span>
        }
        open={passwordModalOpen}
        onCancel={() => {
          setPasswordModalOpen(false);
          setOldPassword('');
          setNewPassword('');
          setConfirmPassword('');
        }}
        footer={null}
        className="profile-modal"
      >
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>Current Password</label>
          <Input.Password
            value={oldPassword}
            onChange={(e) => setOldPassword(e.target.value)}
            placeholder="Enter current password"
            size="large"
          />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>New Password</label>
          <Input.Password
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="Enter new password (min 8 characters)"
            size="large"
          />
        </div>
        <div style={{ marginBottom: 24 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>Confirm New Password</label>
          <Input.Password
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="Confirm new password"
            size="large"
            onPressEnter={() => handleChangePassword()}
          />
        </div>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <Button onClick={() => {
            setPasswordModalOpen(false);
            setOldPassword('');
            setNewPassword('');
            setConfirmPassword('');
          }}>
            Cancel
          </Button>
          <Button
            type="primary"
            loading={passwordLoading}
            onClick={() => handleChangePassword()}
          >
            Change Password
          </Button>
        </div>
      </Modal>

      {/* Set Password Modal (for GitHub OAuth users) */}
      <Modal
        title={
          <span className="modal-title">
            <LockOutlined /> Set Password
          </span>
        }
        open={setPwdModalOpen}
        onCancel={() => {
          setSetPwdModalOpen(false);
          setSetPwdValue('');
          setSetPwdConfirmValue('');
        }}
        footer={null}
        className="profile-modal"
      >
        <div style={{
          padding: '12px',
          background: '#e6f7ff',
          border: '1px solid #91d5ff',
          borderRadius: 4,
          marginBottom: 16
        }}>
          <p style={{ margin: 0, color: '#1890ff' }}>
            Set up a password for your GitHub OAuth account. This will allow you to log in without using GitHub.
          </p>
        </div>
        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>Password</label>
          <Input.Password
            value={setPwdValue}
            onChange={(e) => setSetPwdValue(e.target.value)}
            placeholder="Enter password (min 8 characters)"
            size="large"
          />
        </div>
        <div style={{ marginBottom: 24 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>Confirm Password</label>
          <Input.Password
            value={setPwdConfirmValue}
            onChange={(e) => setSetPwdConfirmValue(e.target.value)}
            placeholder="Confirm password"
            size="large"
            onPressEnter={() => handleSetPassword()}
          />
        </div>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <Button onClick={() => {
            setSetPwdModalOpen(false);
            setSetPwdValue('');
            setSetPwdConfirmValue('');
          }}>
            Cancel
          </Button>
          <Button
            type="primary"
            loading={setPwdLoading}
            onClick={() => handleSetPassword()}
          >
            Set Password
          </Button>
        </div>
      </Modal>

      {/* Delete Account Modal */}
      <Modal
        title={
          <span className="modal-title" style={{ color: '#ff4d4f' }}>
            <DeleteOutlined /> Delete Account
          </span>
        }
        open={deleteModalOpen}
        onCancel={() => {
          setDeleteModalOpen(false);
          setDeletePassword('');
        }}
        footer={null}
        className="profile-modal"
      >
        <div style={{
          padding: '16px',
          background: '#fff2f0',
          border: '1px solid #ffccc7',
          borderRadius: 4,
          marginBottom: 16
        }}>
          <p style={{ margin: 0, color: '#ff4d4f', fontWeight: 500 }}>
            Warning: This action is permanent and cannot be undone!
          </p>
          <p style={{ margin: '8px 0 0', color: '#ff4d4f' }}>
            All your data, including API keys and account information, will be permanently deleted.
          </p>
        </div>
        {!isGithubUser ? (
          <div style={{ marginBottom: 24 }}>
            <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>
              Enter your password to confirm
            </label>
            <Input.Password
              value={deletePassword}
              onChange={(e) => setDeletePassword(e.target.value)}
              placeholder="Enter your password"
              size="large"
            />
          </div>
        ) : (
          <div style={{
            padding: '12px',
            background: '#f6ffed',
            border: '1px solid #b7eb8f',
            borderRadius: 4,
            marginBottom: 24
          }}>
            <p style={{ margin: 0, color: '#52c41a' }}>
              Since you logged in via GitHub, you can delete your account without entering a password.
            </p>
          </div>
        )}
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <Button onClick={() => {
            setDeleteModalOpen(false);
            setDeletePassword('');
          }}>
            Cancel
          </Button>
          <Popconfirm
            title="Are you sure you want to delete your account?"
            description="This action cannot be undone. All your data will be permanently deleted."
            onConfirm={() => handleDeleteAccount()}
            okText="Yes, Delete"
            cancelText="Cancel"
            okButtonProps={{ danger: true, loading: deleteLoading }}
          >
            <Button type="primary" danger loading={deleteLoading}
              disabled={!isGithubUser && !deletePassword}>
              Delete Account
            </Button>
          </Popconfirm>
        </div>
      </Modal>
    </PageContainer>
  );
};

export default Profile;
