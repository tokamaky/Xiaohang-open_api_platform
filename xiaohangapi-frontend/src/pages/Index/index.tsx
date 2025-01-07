import { listInterfaceInfoVoByPageUsingPost } from '@/services/xiaohang-backend/interfaceInfoController';
import { addUserInterfaceInfoUsingPost } from '@/services/xiaohang-backend/userInterfaceInfoController';
import { ActionType, PageContainer, ProColumns, ProTable } from '@ant-design/pro-components';
import { Button, Layout, message } from 'antd';
import Search from 'antd/es/input/Search';
import { Content, Header } from 'antd/es/layout/layout';
import React, { useEffect, useRef, useState } from 'react';
import { history } from 'umi';

const headerStyle: React.CSSProperties = {
    textAlign: 'center',
    height: '64px',
    paddingInline: '30%',
    lineHeight: '64px',
    color: '#fff',
    background: '#fcfcfc',
};

const contentStyle: React.CSSProperties = {
    minHeight: 120,
    lineHeight: '120px',
};


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
            message.error('Request Failed，' + error.message);
        }
        setLoading(false);
    };


  /**
   * Columns for the table display
   */
  const columns: ProColumns<API.InterfaceInfoVO>[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      valueType: 'index',
      align: 'center',
    },
    {
      title: 'Interface Name',
      dataIndex: 'name',
      valueType: 'text',
      align: 'center',
    },
    {
      title: 'Description',
      dataIndex: 'description',
      valueType: 'textarea',
      align: 'center',
    },
    {
      title: 'Request Method',
      dataIndex: 'method',
      valueType: 'text',
      align: 'center',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      hideInForm: true,
      valueEnum: {
        0: {
          text: 'Inactive',
          status: 'Default',
        },
        1: {
          text: 'Active',
          status: 'Processing',
        },
      },
      align: 'center',
    },
    {
      title: 'Creation Time',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      align: 'center',
    },
    {
      title: 'Actions',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => {
        return record.isOwnerByCurrentUser ? (
          <Button
            type="primary"
            key="onlineUse"
            onClick={() => {
              history.push(`/interface_info/${record.id}`);
            }}
          >
            Online Invocation
          </Button>
        ) : (
          <Button
            key="applyInterface"
            onClick={async () => {
              const res = await addUserInterfaceInfoUsingPost({
                interfaceInfoId: Number(record.id),
              });
              if (res.code === 0) {
                message.success('Request Successful');
                // Refresh table
                await loadData();
              }
            }}
          >
            Activate Interface
          </Button>
        );
      },
    },
  ];

  useEffect(() => {
    loadData();
  }, []);

  const onSearch = (value: string) => {
    loadData(value);
  };

  return (
    <PageContainer>
      <Layout>
        <Header style={headerStyle}>
          <Search
            size={'large'}
            placeholder="Enter interface name or description"
            onSearch={onSearch}
            enterButton
          />
        </Header>
        <Content style={contentStyle}>
          <ProTable<API.RequestParamsRemarkVO>
            rowKey="id"
            toolBarRender={false}
            columns={columns}
            dataSource={list}
            loading={loading}
            actionRef={ref}
            pagination={{
              showTotal: (total) => {
                return 'Total: ' + total;
              },
              total,
              pageSize: 10,
              onChange: (page, pageSize) => {
                loadData('', page, pageSize);
              },
            }}
            search={false}
          />
        </Content>
      </Layout>
    </PageContainer>
  );

};

export default Index;
