import {
    getInterfaceInfoVoByIdUsingGet,
    invokeInterfaceInfoUsingPost,
} from '@/services/xiaohang-backend/interfaceInfoController';
import { useParams } from '@@/exports';
import { PageContainer } from '@ant-design/pro-components';
import { Badge, Button, Card, Descriptions, Divider, Form, Input, message, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import moment from 'moment';
import React, { useEffect, useState } from 'react';

const requestColumns: ColumnsType<API.RequestParamsRemarkVO> = [
  {
    title: 'Name',
    dataIndex: 'name',
    width: '100px',
  },
  {
    title: 'Required',
    key: 'isRequired',
    dataIndex: 'isRequired',
    width: '100px',
  },
  {
    title: 'Type',
    dataIndex: 'type',
    width: '100px',
  },
  {
    title: 'Description',
    dataIndex: 'remark',
  },
];

const responseColumns: ColumnsType<API.RequestParamsRemarkVO> = [
  {
    title: 'Name',
    dataIndex: 'name',
    width: '100px',
  },
  {
    title: 'Type',
    dataIndex: 'type',
    width: '100px',
  },
  {
    title: 'Description',
    dataIndex: 'remark',
  },
];

const Index: React.FC = () => {
  const [invokeLoading, setInvokeLoading] = useState(false);
  const [data, setData] = useState<API.InterfaceInfoVO>();
  const params = useParams();
  const [invokeRes, setInvokeRes] = useState<any>();

  const loadData = async () => {
    try {
      const interfaceInfoRes = await getInterfaceInfoVoByIdUsingGet({
        id: Number(params.id),
      });

      const interfaceInfoData = interfaceInfoRes.data;

      if (interfaceInfoData) {
        setData(interfaceInfoData);
      }
    } catch (error: any) {
      message.error('Request Failed, ' + error.message);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const onFinish = async (values: any) => {
    console.log('values:', values);
    if (!params.id) {
      message.error('Interface not found');
      return;
    }
    setInvokeLoading(true);
    try {
      const res = await invokeInterfaceInfoUsingPost({
        id: params.id,
        ...values,
      });
      console.log('API request data:', res);
      if (res.data) {
        res.data = res.data.replace(/\\/g, '');
        setInvokeRes(res.data);
        message.success('API request successful');
      } else {
        const messageObj = JSON.parse(res.message as string);
        message.error(messageObj.message);
      }
    } catch (error: any) {
      message.error('API Request Failed');
    }
    setInvokeLoading(false);
  };

  return (
    <PageContainer>
      <Card>
        {data ? (
          <Descriptions title={data.name} column={4} layout={'vertical'}>
            <Descriptions.Item label="Description">{data.description}</Descriptions.Item>
            <Descriptions.Item label="API Status">
              {data.status ? (
                <Badge status="success" text={'Active'} />
              ) : (
                <Badge status="default" text={'Inactive'} />
              )}
            </Descriptions.Item>
            <Descriptions.Item label="Host">{data.host}</Descriptions.Item>
            <Descriptions.Item label="Request URL">{data.url}</Descriptions.Item>
            <Descriptions.Item label="Request Method">{data.method}</Descriptions.Item>
            <Descriptions.Item label="Request Parameter Example" span={4}>
              {data.requestParams}
            </Descriptions.Item>
            <Descriptions.Item label="Request Parameters Description" span={4}>
              <Table
                style={{ width: '100%' }}
                pagination={{
                  hideOnSinglePage: true,
                }}
                columns={requestColumns}
                dataSource={data.requestParamsRemark}
              />
            </Descriptions.Item>
            <Descriptions.Item label="Response Parameters Description" span={4}>
              <Table
                style={{ width: '100%' }}
                pagination={{
                  hideOnSinglePage: true,
                }}
                columns={responseColumns}
                dataSource={data.responseParamsRemark}
              />
            </Descriptions.Item>
            <Descriptions.Item label="Request Headers">{data.requestHeader}</Descriptions.Item>
            <Descriptions.Item label="Response Headers">{data.responseHeader}</Descriptions.Item>
            <Descriptions.Item label="Creation Time">
              {moment(data.createTime).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
            <Descriptions.Item label="Update Time">
              {moment(data.updateTime).format('YYYY-MM-DD HH:mm:ss')}
            </Descriptions.Item>
          </Descriptions>
        ) : (
          <>Interface not found</>
        )}
      </Card>
      {data ? (
        <>
          <Divider />
          <Card title={'Online Test'}>
            <Form name="invoke" layout={'vertical'} onFinish={onFinish}>
              <Form.Item
                label={'Request Parameters'}
                initialValue={data?.requestParams}
                name={'requestParams'}
              >
                <Input.TextArea defaultValue={data?.requestParams} rows={6} />
              </Form.Item>

              <Form.Item wrapperCol={{ span: 16 }}>
                <Button type="primary" htmlType="submit">
                  Invoke
                </Button>
              </Form.Item>
            </Form>
          </Card>
          <Divider />
          <Card title={'Response Results'} loading={invokeLoading}>
            <Input.TextArea value={invokeRes} rows={10} />
          </Card>
        </>
      ) : null}
    </PageContainer>
  );
};

export default Index;
