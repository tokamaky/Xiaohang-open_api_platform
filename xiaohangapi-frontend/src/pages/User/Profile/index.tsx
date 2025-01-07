import {
    getUserVoByIdUsingGet,
    updateSecretKeyUsingPost,
    updateUserUsingPost,
    userLoginUsingPost,
} from '@/services/xiaohang-backend/userController';
import {useModel} from '@@/exports';
import {
    CommentOutlined,
    FieldTimeOutlined,
    LoadingOutlined,
    LockOutlined,
    PlusOutlined,
    UnlockOutlined,
    UserOutlined,
    VerifiedOutlined,
} from '@ant-design/icons';
import {PageContainer, ProForm, ProFormInstance, ProFormText} from '@ant-design/pro-components';
import {Button, Card, Col, Divider, message, Modal, Row, Typography, Upload, UploadFile, UploadProps,} from 'antd';
import {RcFile, UploadChangeParam} from 'antd/es/upload';
import React, {useEffect, useRef, useState} from 'react';
//import {uploadFileUsingPOST} from "@/services/xiaohang-backend/fileController";

const { Paragraph } = Typography;

const avatarStyle: React.CSSProperties = {
    width: '100%',
    textAlign: 'center',
};
const buttonStyle: React.CSSProperties = {
    marginLeft: '30px',
};
/**
 * Pre-upload validation
 * @param file The file
 */
const beforeUpload = (file: RcFile) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('Only JPG/PNG format files are allowed!');
  }
  const isLt2M = file.size / 1024 / 1024 < 5;
  if (!isLt2M) {
    message.error('The maximum upload size is 5MB!');
  }
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

  const getUserInfo = async (id: any) => {
    return getUserVoByIdUsingGet({ id }).then((res) => {
      if (res.data) {
        setInitialState((s: any) => ({ ...s, loginUser: res.data }));
        setData(res.data);
        setImageUrl(res.data.userAvatar);
      }
    });
  };

  useEffect(() => {
    try {
      getUserInfo(initialState?.loginUser?.id);
    } catch (e: any) {
      console.log(e);
    }
  }, []);


  // Show secret key
  const showSecretKey = async () => {
    let userPassword = formRef?.current?.getFieldValue('userPassword');

    // Login
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

  // Update user avatar
  const updateUserAvatar = async (id: number, userAvatar: string) => {
    const res = await updateUserUsingPost({
      id,
      userAvatar,
    });
    if (res.code !== 0) {
      message.success(`Failed to update user avatar`);
    } else {
      getUserInfo(id);
    }
  };

  /**
   * Handle image upload
   * @param info
   */
  const handleChange: UploadProps['onChange'] = (info: UploadChangeParam<UploadFile>) => {
    if (info.file.status === 'uploading') {
      setLoading(true);
      return;
    }
    if (info.file.status === 'done') {
      if (info.file.response.code === 0) {
        message.success(`Upload successful`);
        const id = initialState?.loginUser?.id as number;
        const userAvatar = info.file.response.data.url;
        setLoading(false);
        setImageUrl(userAvatar);
        updateUserAvatar(id, userAvatar);
      }
    }
  };

  const uploadButton = (
    <div>
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>Upload</div>
    </div>
  );

  // Reset secret key
  const resetSecretKey = async () => {
    try {
      let userPassword = formRef?.current?.getFieldValue('userPassword');
      const res = await userLoginUsingPost({
        userAccount: data?.userAccount,
        userPassword: userPassword,
      });
      if (res.code === 0) {
        const res = await updateSecretKeyUsingPost({
          id: data?.id,
        });
        if (res.data) {
          getUserInfo(data?.id);
          message.success('Reset successful!');
          setOpen(false);
        }
      }
    } catch (e: any) {
      console.log(e);
    }
  };

  return (
    <PageContainer>
      <Row gutter={24}>
        <Col span={8}>
          <Card title="Personal Information" bordered={false}>
            <Row>
              <Col style={avatarStyle}>
                <Upload
                  name="file"
                  listType="picture-circle"
                  showUploadList={false}
                  action="http://124.70.63.241:8101/api/file/upload"
                  beforeUpload={beforeUpload}
                  onChange={handleChange}
                >
                  {imageUrl ? (
                    <img
                      src={data?.userAvatar}
                      alt="avatar"
                      style={{ width: '100%', borderRadius: '50%' }}
                    />
                  ) : (
                    uploadButton
                  )}
                </Upload>
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                <UserOutlined /> Username: {data?.userName}
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                <CommentOutlined /> User Account: {data?.userAccount}
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                <VerifiedOutlined /> User Role: {data?.userRole}
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                <FieldTimeOutlined /> Registration Time: {data?.createTime}
              </Col>
            </Row>
          </Card>
        </Col>
        <Col span={16}>
          <Card title="Secret Key Operations" bordered={false}>
            <Row>
              <Col>
                {visible ? (
                  <Paragraph
                    copyable={{
                      text: data?.accessKey,
                    }}
                  >
                    <LockOutlined /> accessKey: {data?.accessKey}
                  </Paragraph>
                ) : (
                  <Paragraph>
                    <UnlockOutlined /> secretKey: *********
                  </Paragraph>
                )}
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                {visible ? (
                  <Paragraph
                    copyable={{
                      text: data?.secretKey,
                    }}
                  >
                    <UnlockOutlined /> secretKey: {data?.secretKey}
                  </Paragraph>
                ) : (
                  <Paragraph>
                    <UnlockOutlined /> secretKey: *********
                  </Paragraph>
                )}
              </Col>
            </Row>
            <Divider />
            <Row>
              <Col>
                {!visible ? (
                  <Button
                    type="primary"
                    onClick={() => {
                      setOpen(true);
                      setFlag(true);
                    }}
                  >
                    View Secret Key
                  </Button>
                ) : (
                  <Button type="primary" onClick={() => setVisible(false)}>
                    Hide Secret Key
                  </Button>
                )}
                <Button
                  style={buttonStyle}
                  onClick={() => {
                    setOpen(true);
                    setFlag(false);
                  }}
                  type="primary"
                  danger
                >
                  Reset Secret Key
                </Button>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
      <Modal
        title="View Secret Key"
        open={open}
        onOk={flag ? showSecretKey : resetSecretKey}
        onCancel={() => setOpen(false)}
      >
        <ProForm<{ userPassword: string }>
          formRef={formRef}
          formKey="check-user-password-form"
          autoFocusFirstInput
          submitter={{
            resetButtonProps: {
              style: {
                display: 'none',
              },
            },
            submitButtonProps: {
              style: {
                display: 'none',
              },
            },
          }}
        >
          <ProFormText.Password name="userPassword" placeholder="Please enter user password" />
        </ProForm>
      </Modal>
    </PageContainer>
  );
};

export default Profile;
