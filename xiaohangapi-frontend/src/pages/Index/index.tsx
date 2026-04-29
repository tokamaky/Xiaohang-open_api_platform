import { listInterfaceInfoVoByPageUsingPost } from '@/services/xiaohang-backend/interfaceInfoController';
import { addUserInterfaceInfoUsingPost } from '@/services/xiaohang-backend/userInterfaceInfoController';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import Search from 'antd/es/input/Search';
import React, { useEffect, useRef, useState } from 'react';
import { history } from 'umi';
import './index.less';

const Index: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [list, setList] = useState<API.InterfaceInfoVO[]>([]);
  const [total, setTotal] = useState<number>(0);
  const ref = useRef<ActionType>();

  const loadData = async (searchText = '', current = 1, pageSize = 10) => {
    setLoading(true);
    try {
      await listInterfaceInfoVoByPageUsingPost({
        searchText,
        current,
        pageSize,
      }).then((res) => {
        setList(res?.data?.records ?? []);
        setTotal(res?.data?.total ?? 0);
      });
    } catch (error: any) {
      message.error('Request Failed: ' + error.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, []);

  const columns: ProColumns<API.InterfaceInfoVO>[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      valueType: 'index',
      align: 'center',
      width: 80,
    },
    {
      title: 'Interface Name',
      dataIndex: 'name',
      valueType: 'text',
      align: 'center',
      render: (_, record) => (
        <span className="api-name-cell">{record.name}</span>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      valueType: 'text',
      align: 'center',
      ellipsis: true,
    },
    {
      title: 'Method',
      dataIndex: 'method',
      valueType: 'text',
      align: 'center',
      width: 100,
      render: (_, record) => {
        const method = (record.method || 'GET').toUpperCase();
        const isGet = method === 'GET';
        const isPost = method === 'POST';
        const isPut = method === 'PUT';
        const isDelete = method === 'DELETE';
        let cls = 'method-badge method-default';
        if (isGet) cls = 'method-badge method-get';
        else if (isPost) cls = 'method-badge method-post';
        else if (isPut) cls = 'method-badge method-put';
        else if (isDelete) cls = 'method-badge method-delete';
        return <span className={cls}>{method}</span>;
      },
    },
    {
      title: 'Status',
      dataIndex: 'status',
      hideInForm: true,
      valueEnum: {
        0: { text: 'Offline', status: 'Default' },
        1: { text: 'Online', status: 'Processing' },
      },
      align: 'center',
      width: 100,
      render: (_, record) => {
        const online = record.status === 1;
        return (
          <span className={`status-indicator ${online ? 'status-online' : 'status-offline'}`}>
            <span className="status-dot" />
            {online ? 'Online' : 'Offline'}
          </span>
        );
      },
    },
    {
      title: 'Created',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      align: 'center',
      width: 170,
    },
    {
      title: 'Actions',
      dataIndex: 'option',
      valueType: 'option',
      align: 'center',
      width: 200,
      render: (_, record) => {
        return record.isOwnerByCurrentUser ? (
          <Button
            className="action-btn"
            size="small"
            onClick={() => history.push(`/interface_info/${record.id}`)}
          >
            Online Invocation
          </Button>
        ) : (
          <Button
            className="action-btn action-btn-invoke"
            size="small"
            onClick={async () => {
              const res = await addUserInterfaceInfoUsingPost({
                interfaceInfoId: Number(record.id),
              });
              if (res.code === 0) {
                message.success('Activated successfully');
                loadData();
              }
            }}
          >
            Activate
          </Button>
        );
      },
    },
  ];

  return (
    <PageContainer>
      <div className="home-page">
        {/* Terminal-style search */}
        <div className="home-search-bar">
          <div className="search-terminal">
            <div className="search-terminal-header">
              <span className="terminal-dots">
                <span />
                <span />
                <span />
              </span>
              <span className="terminal-title">Search APIs — Type to filter</span>
              <span className="terminal-live-dot" />
            </div>
            <div className="search-terminal-body">
              <Search
                size="large"
                placeholder="Search interface name or description..."
                onSearch={(value) => loadData(value)}
                enterButton={
                  <span className="search-enter-btn">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                      <circle cx="11" cy="11" r="8" /><path d="m21 21-4.35-4.35" />
                    </svg>
                    Search
                  </span>
                }
              />
            </div>
          </div>
          <p className="search-hint">
            Showing <strong>{total}</strong> available API{total !== 1 ? 's' : ''} in the marketplace
          </p>
        </div>

        {/* API Table */}
        <div className="home-table-wrap">
          <ProTable<API.InterfaceInfoVO>
            rowKey="id"
            toolBarRender={false}
            columns={columns}
            dataSource={list}
            loading={loading}
            actionRef={ref}
            pagination={{
              showTotal: (t) => `${t} APIs`,
              total,
              pageSize: 10,
              onChange: (page, pageSize) => loadData('', page, pageSize),
            }}
            search={false}
          />
        </div>
      </div>
    </PageContainer>
  );
};

export default Index;
