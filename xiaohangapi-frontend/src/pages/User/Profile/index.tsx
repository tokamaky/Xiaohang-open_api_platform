import {
    updateSecretKeyUsingPost,
    updateUserUsingPost,
    userLoginUsingPost,
    getGithubAuthUrlUsingGet,
    bindGithubUsingPost,
    unbindGithubUsingPost,
    getLoginUserUsingGet,
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
} from '@ant-design/icons';
import {PageContainer, ProForm, ProFormInstance, ProFormText} from '@ant-design/pro-components';
import {Button, Card, Divider, message, Modal, Typography, Upload, UploadFile, UploadProps, Tag} from 'antd';
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

const Profile: React.FC = () => {
  const [data, setData] = useState<API.UserVO>({});
  const [visible, setVisible] = useState<boolean>(false);
  const [flag, setFlag] = useState<boolean>(false);
  const [open, setOpen] = useState<boolean>(false);
  const [loading, setLoading] = useState(false);
  const [imageUrl, setImageUrl] = useState<string>();
  const { initialState, setInitialState } = useModel('@@initialState');
  const formRef = useRef<ProFormInstance<{ userPassword: string }>>();

  const getUserInfo = async () => {
    return getLoginUserUsingGet().then((res) => {
      if (res.data) {
        setInitialState((s: any) => ({ ...s, loginUser: res.data }));
        setData(res.data as API.UserVO);
        setImageUrl(res.data.userAvatar);
      }
    });
  };

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('__oauth_done') === '1') {
      getUserInfo();
      const cleanUrl = window.location.pathname;
      window.history.replaceState({}, '', cleanUrl);
    }
  }, []);

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
              <span className="info-value">{data?.userName || '—'}</span>
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
                        if (res.code === 0) {
                          message.success('GitHub account unlinked');
                          const newData = { ...data, githubId: undefined as any };
                          setData(newData);
                          setInitialState((s: any) => ({ ...s, loginUser: newData }));
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
                        const currentUrl = encodeURIComponent(window.location.href.split('?')[0]);
                        const res = await (window as any).fetch(
                          `https://backend-production-796b.up.railway.app/api/oauth/github/url?redirectUrl=${currentUrl}`
                        ).then(r => r.json());
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
    </PageContainer>
  );
};

export default Profile;
