import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import { Button, Card, Descriptions, Form, message, Input, Spin, Divider } from 'antd';
import {
  getInterfaceInfoByIdUsingGet,
  invokeInterfaceInfoUsingPost,
} from '@/services/xiaohangapi-backend/interfaceInfoController';
import { useParams } from '@@/exports';

/**
 * Homepage
 * @constructor
 */
const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<API.InterfaceInfo>();
  const [invokeRes, setInvokeRes] = useState<any>();
  const [invokeLoading, setInvokeLoading] = useState(false);

  const params = useParams();

  const loadData = async () => {
    if (!params.id) {
      message.error('Parameter does not exist');
      return;
    }
    setLoading(true);
    try {
      const res = await getInterfaceInfoByIdUsingGet({
        id: Number(params.id),
      });
      setData(res.data);
    } catch (error: any) {
      message.error('Operation failed, ' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const onFinish = async (values: any) => {
    if (!params.id) {
      message.error('Interface does not exist');
      return;
    }
    setInvokeLoading(true);
    try {
      const res = await invokeInterfaceInfoUsingPost({
        id: params.id,
        ...values,
      });
      setInvokeRes(res.data);
      message.success('Request successful');
    } catch (error: any) {
      message.error('Operation failed, ' + error.message);
    }
    setInvokeLoading(false);
  };

  return (
    <PageContainer title="View Interface Documentation">
      <Card>
        {data ? (
          <Descriptions title={data.name} column={1}>
            <Descriptions.Item label="Interface Status">{data.status ? 'Enabled' : 'Disabled'}</Descriptions.Item>
            <Descriptions.Item label="Description">{data.description}</Descriptions.Item>
            <Descriptions.Item label="Request URL">{data.url}</Descriptions.Item>
            <Descriptions.Item label="Request Method">{data.method}</Descriptions.Item>
            <Descriptions.Item label="Request Parameters">{data.requestParams}</Descriptions.Item>
            <Descriptions.Item label="Request Header">{data.requestHeader}</Descriptions.Item>
            <Descriptions.Item label="Response Header">{data.responseHeader}</Descriptions.Item>
            <Descriptions.Item label="Created Time">{data.createTime}</Descriptions.Item>
            <Descriptions.Item label="Updated Time">{data.updateTime}</Descriptions.Item>
          </Descriptions>
        ) : (
          <>Interface does not exist</>
        )}
      </Card>
      <Divider />
      <Card title="Online Test">
        <Form name="invoke" layout="vertical" onFinish={onFinish}>
          <Form.Item label="Request Parameters" name="userRequestParams">
            <Input.TextArea />
          </Form.Item>
          <Form.Item wrapperCol={{ span: 16 }}>
            <Button type="primary" htmlType="submit">
              Invoke
            </Button>
          </Form.Item>
        </Form>
      </Card>
      <Divider />
      <Card title="Response Result" loading={invokeLoading}>
        {invokeRes}
      </Card>
    </PageContainer>
  );
};

export default Index;
