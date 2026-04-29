import {
    getInterfaceInfoVoByIdUsingGet,
    invokeInterfaceInfoUsingPost,
} from '@/services/xiaohang-backend/interfaceInfoController';
import { useParams } from '@@/exports';
import { PageContainer } from '@ant-design/pro-components';
import { Badge, Button, Card, Descriptions, Divider, Form, Input, message, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import moment from 'moment';
import React, { useEffect, useState } from 'react';
import './index.less';

const requestColumns: ColumnsType<API.RequestParamsRemarkVO> = [
  { title: 'Name', dataIndex: 'name', width: 160 },
  { title: 'Required', dataIndex: 'isRequired', width: 100,
    render: (v) => (
      <span className={`req-badge ${v ? 'req-required' : 'req-optional'}`}>
        {v ? 'Yes' : 'No'}
      </span>
    )
  },
  { title: 'Type', dataIndex: 'type', width: 120 },
  { title: 'Description', dataIndex: 'remark' },
];

const responseColumns: ColumnsType<API.RequestParamsRemarkVO> = [
  { title: 'Name', dataIndex: 'name', width: 160 },
  { title: 'Type', dataIndex: 'type', width: 120 },
  { title: 'Description', dataIndex: 'remark' },
];

const Index: React.FC = () => {
  const [invokeLoading, setInvokeLoading] = useState(false);
  const [data, setData] = useState<API.InterfaceInfoVO>();
  const params = useParams();
  const [invokeRes, setInvokeRes] = useState<any>();
  const [invokeStatus, setInvokeStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [form] = Form.useForm();

  const loadData = async () => {
    try {
      const res = await getInterfaceInfoVoByIdUsingGet({ id: Number(params.id) });
      if (res.data) setData(res.data);
    } catch (error: any) {
      message.error('Failed to load: ' + error.message);
    }
  };

  useEffect(() => { loadData(); }, []);

  const onFinish = async (values: any) => {
    setInvokeLoading(true);
    setInvokeStatus('idle');
    setInvokeRes('');
    try {
      const res = await invokeInterfaceInfoUsingPost({ id: params.id, ...values });
      if (res.data) {
        let formatted = res.data.replace(/\\/g, '');
        try {
          const parsed = JSON.parse(formatted);
          formatted = JSON.stringify(parsed, null, 2);
        } catch { /* keep raw */ }
        setInvokeRes(formatted);
        setInvokeStatus('success');
        message.success('API invoked successfully');
      } else {
        const msgObj = JSON.parse(res.message as string);
        message.error(msgObj.message || 'Invoke failed');
        setInvokeStatus('error');
        setInvokeRes(JSON.stringify({ error: msgObj.message || 'Unknown error' }, null, 2));
      }
    } catch (error: any) {
      message.error('Request failed');
      setInvokeStatus('error');
      setInvokeRes(JSON.stringify({ error: 'Network error or server unreachable' }, null, 2));
    }
    setInvokeLoading(false);
  };

  return (
    <PageContainer>
      <div className="interface-info-page">
        {/* API Overview Card */}
        {data && (
          <Card className="info-card" bordered={false}>
            <div className="info-header">
              <div className="info-title-row">
                <span className={`method-badge method-${(data.method || 'get').toLowerCase()}`}>
                  {data.method?.toUpperCase() || 'GET'}
                </span>
                <h2 className="info-name">{data.name}</h2>
                <Badge
                  status={data.status ? 'success' : 'default'}
                  text={<span className={data.status ? 'status-online' : 'status-offline'}>{data.status ? 'Online' : 'Offline'}</span>}
                />
              </div>
              <p className="info-desc">{data.description || 'No description provided.'}</p>
            </div>

            <Descriptions className="info-descriptions" column={4} layout="vertical">
              <Descriptions.Item label="Host">
                <code className="info-code">{data.host}</code>
              </Descriptions.Item>
              <Descriptions.Item label="Endpoint">
                <code className="info-code">{data.url}</code>
              </Descriptions.Item>
              <Descriptions.Item label="Created">
                {data.createTime ? moment(data.createTime).format('YYYY-MM-DD') : '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Updated">
                {data.updateTime ? moment(data.updateTime).format('YYYY-MM-DD') : '—'}
              </Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        {/* Parameters */}
        {data?.requestParamsRemark?.length ? (
          <Card className="info-card" title={<><span className="card-title-accent">///</span> Request Parameters</>} bordered={false}>
            <Table
              className="params-table"
              columns={requestColumns}
              dataSource={data.requestParamsRemark}
              pagination={{ hideOnSinglePage: true }}
              rowKey="name"
              size="small"
            />
          </Card>
        ) : null}

        {data?.responseParamsRemark?.length ? (
          <Card className="info-card" title={<><span className="card-title-accent">///</span> Response Parameters</>} bordered={false}>
            <Table
              className="params-table"
              columns={responseColumns}
              dataSource={data.responseParamsRemark}
              pagination={{ hideOnSinglePage: true }}
              rowKey="name"
              size="small"
            />
          </Card>
        ) : null}

        {/* Invoke Card */}
        {data ? (
          <Card className="info-card invoke-card" bordered={false}>
            <div className="invoke-header">
              <div className="invoke-title">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
                Online Invocation
              </div>
              <p className="invoke-hint">Fill in parameters and hit Invoke to test the API in real-time.</p>
            </div>

            <Form form={form} layout="vertical" onFinish={onFinish} className="invoke-form">
              <Form.Item
                label="Request Parameters (JSON)"
                name="requestParams"
                initialValue={data.requestParams}
              >
                <Input.TextArea
                  className="invoke-input"
                  rows={8}
                  placeholder='{"key": "value"}'
                  spellCheck={false}
                />
              </Form.Item>

              <Form.Item>
                <Space size={12}>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={invokeLoading}
                    className="invoke-btn"
                    icon={
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <polygon points="5 3 19 12 5 21 5 3" />
                      </svg>
                    }
                  >
                    Invoke
                  </Button>
                  {invokeRes && (
                    <Button
                      onClick={() => {
                        navigator.clipboard.writeText(invokeRes);
                        message.success('Response copied to clipboard');
                      }}
                    >
                      Copy Response
                    </Button>
                  )}
                </Space>
              </Form.Item>
            </Form>

            {invokeRes && (
              <div className="response-wrap">
                <div className={`response-header response-${invokeStatus}`}>
                  <span className="response-label">
                    {invokeStatus === 'success' ? 'Response (200 OK)' : 'Response (Error)'}
                  </span>
                  <span className={`response-status response-status-${invokeStatus}`}>
                    {invokeStatus === 'success' ? '200' : 'ERR'}
                  </span>
                </div>
                <Input.TextArea
                  className="invoke-input response-output"
                  value={invokeRes}
                  rows={12}
                  readOnly
                  spellCheck={false}
                />
              </div>
            )}
          </Card>
        ) : (
          <Card className="info-card" bordered={false}>
            <p style={{ color: 'var(--text-secondary)', textAlign: 'center', padding: '40px 0' }}>
              Interface not found.
            </p>
          </Card>
        )}
      </div>
    </PageContainer>
  );
};

export default Index;
