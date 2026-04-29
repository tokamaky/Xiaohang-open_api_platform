import CreateModal from '@/pages/Admin/InterfaceInfo/components/CreateModal';
import ShowModal from '@/pages/Admin/InterfaceInfo/components/ShowModal';
import UpdateModal from '@/pages/Admin/InterfaceInfo/components/UpdateModal';
import {
    addInterfaceInfoUsingPost,
    deleteInterfaceInfoUsingPost,
    listInterfaceInfoVoByPageUsingPost,
    offlineInterfaceInfoUsingPost,
    onlineInterfaceInfoUsingPost,
    updateInterfaceInfoUsingPost,
} from '@/services/xiaohang-backend/interfaceInfoController';
import { PlusOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Button, message, Popconfirm } from 'antd';
import React, { useRef, useState } from 'react';
import './index.less';

const TableList: React.FC = () => {
  const [createModalOpen, handleModalOpen] = useState<boolean>(false);
  const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);
  const [showModalOpen, handleShowModalOpen] = useState<boolean>(false);
  const [showDetail, setShowDetail] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.InterfaceInfoVO>();

  const handleAdd = async (fields: API.InterfaceInfoVO) => {
    const hide = message.loading('Adding...');
    try {
      await addInterfaceInfoUsingPost({ ...fields });
      hide();
      message.success('Created successfully');
      handleModalOpen(false);
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('Failed: ' + error.message);
      return false;
    }
  };

  const handleUpdate = async (fields: API.InterfaceInfoVO) => {
    if (!currentRow) return;
    const hide = message.loading('Updating...');
    try {
      await updateInterfaceInfoUsingPost({ id: currentRow.id, ...fields });
      hide();
      message.success('Updated successfully');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('Failed: ' + error.message);
      return false;
    }
  };

  const handleRemove = async (record: API.InterfaceInfoVO) => {
    const hide = message.loading('Deleting...');
    if (!record) return true;
    try {
      await deleteInterfaceInfoUsingPost({ id: record.id });
      hide();
      message.success('Deleted successfully');
      actionRef.current?.reload();
      return true;
    } catch {
      hide();
      message.error('Failed to delete');
      return false;
    }
  };

  const handleOnline = async (record: API.InterfaceInfoVO) => {
    const hide = message.loading('Publishing...');
    try {
      await onlineInterfaceInfoUsingPost({
        host: record.host, id: record.id, method: record.method,
        requestParams: record.requestParams,
      });
      hide();
      message.success('Published successfully');
      actionRef.current?.reload();
    } catch {
      hide();
      message.error('Publish failed');
    }
  };

  const handleOffline = async (record: API.InterfaceInfoVO) => {
    const hide = message.loading('Taking offline...');
    try {
      await offlineInterfaceInfoUsingPost({ id: record.id });
      hide();
      message.success('Taken offline');
      actionRef.current?.reload();
    } catch {
      hide();
      message.error('Failed');
    }
  };

  const columns: ProColumns<API.InterfaceInfoVO>[] = [
    {
      title: 'ID', dataIndex: 'id', valueType: 'index', width: 70,
    },
    {
      title: 'Interface Name', dataIndex: 'name', valueType: 'text',
      render: (_, record) => <span className="admin-api-name">{record.name}</span>,
    },
    {
      title: 'Method', dataIndex: 'method', valueType: 'text', width: 90,
      render: (_, record) => {
        const m = (record.method || 'GET').toUpperCase();
        return <span className={`method-badge method-${m.toLowerCase()}`}>{m}</span>;
      },
    },
    {
      title: 'Status', dataIndex: 'status', hideInForm: true,
      valueEnum: {
        0: { text: 'Offline', status: 'Default' },
        1: { text: 'Online', status: 'Processing' },
      },
      width: 100,
      render: (_, record) => (
        <span className={`status-indicator ${record.status === 1 ? 'status-online' : 'status-offline'}`}>
          <span className="status-dot" />
          {record.status === 1 ? 'Online' : 'Offline'}
        </span>
      ),
    },
    {
      title: 'Created', dataIndex: 'createTime', valueType: 'dateTime', hideInForm: true, width: 170,
    },
    {
      title: 'Actions', dataIndex: 'option', valueType: 'option', width: 280,
      render: (_, record) => record.status === 0 ? [
        <Button key="detail" size="small" className="admin-btn" onClick={() => { handleShowModalOpen(true); setCurrentRow(record); }}>Detail</Button>,
        <Button key="update" size="small" className="admin-btn" onClick={() => { handleUpdateModalOpen(true); setCurrentRow(record); }}>Edit</Button>,
        <Button key="online" size="small" className="admin-btn admin-btn-publish" onClick={() => handleOnline(record)}>Publish</Button>,
        <Popconfirm title="Delete this interface?" description="This action cannot be undone." okText="Delete" okButtonProps={{ danger: true }} icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          onConfirm={() => handleRemove(record)}>
          <Button size="small" className="admin-btn admin-btn-danger" danger>Delete</Button>
        </Popconfirm>,
      ] : [
        <Button key="detail" size="small" className="admin-btn" onClick={() => { handleShowModalOpen(true); setCurrentRow(record); }}>Detail</Button>,
        <Button key="update" size="small" className="admin-btn" onClick={() => { handleUpdateModalOpen(true); setCurrentRow(record); }}>Edit</Button>,
        <Button key="offline" size="small" className="admin-btn" onClick={() => handleOffline(record)}>Offline</Button>,
        <Popconfirm title="Delete this interface?" description="This action cannot be undone." okText="Delete" okButtonProps={{ danger: true }} icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
          onConfirm={() => handleRemove(record)}>
          <Button size="small" className="admin-btn admin-btn-danger" danger>Delete</Button>
        </Popconfirm>,
      ],
    },
  ];

  const requestColumns: ProColumns<API.RequestParamsRemarkVO>[] = [
    { title: 'Name', dataIndex: 'name', width: '15%' },
    { title: 'Required', key: 'isRequired', dataIndex: 'isRequired', valueType: 'select',
      valueEnum: { yes: { text: 'Yes' }, no: { text: 'No' } }, width: '15%' },
    { title: 'Type', dataIndex: 'type', width: '15%' },
    { title: 'Description', dataIndex: 'remark' },
    { title: 'Actions', valueType: 'option', width: '10%', render: () => null },
  ];

  const responseColumns: ProColumns<API.RequestParamsRemarkVO>[] = [
    { title: 'Name', dataIndex: 'name', width: '15%' },
    { title: 'Type', dataIndex: 'type', width: '15%' },
    { title: 'Description', dataIndex: 'remark' },
    { title: 'Actions', valueType: 'option', width: '10%', render: () => null },
  ];

  return (
    <PageContainer>
      <div className="admin-page">
        <ProTable<API.InterfaceInfoVO, API.PageParams>
          className="admin-table"
          headerTitle={
            <div className="table-header-title">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="18" height="18" rx="2" /><path d="M3 9h18M9 21V9" />
              </svg>
              API Management
            </div>
          }
          actionRef={actionRef}
          rowKey="id"
          search={{ labelWidth: 120 }}
          toolBarRender={() => [
            <Button
              key="create"
              type="primary"
              className="admin-btn admin-btn-create"
              onClick={() => handleModalOpen(true)}
            >
              <PlusOutlined /> Create Interface
            </Button>,
          ]}
          request={async (params) => {
            const res = await listInterfaceInfoVoByPageUsingPost({ ...params });
            return res?.data ? {
              data: res.data.records ?? [],
              success: true,
              total: res.data.total ?? 0,
            } : { data: [], success: false, total: 0 };
          }}
          columns={columns}
        />
      </div>

      <UpdateModal
        onSubmit={async (value) => {
          const success = await handleUpdate(value);
          if (success) { handleUpdateModalOpen(false); setCurrentRow(undefined); actionRef.current?.reload(); }
        }}
        setVisible={handleUpdateModalOpen}
        visible={updateModalOpen}
        values={currentRow ?? {}}
        columns={columns}
        requestColumns={requestColumns}
        responseColumns={responseColumns}
      />

      <ShowModal
        setVisible={handleShowModalOpen}
        values={currentRow ?? {}}
        visible={showModalOpen}
        requestColumns={requestColumns}
        responseColumns={responseColumns}
      />

      <CreateModal
        columns={columns}
        setVisible={handleModalOpen}
        onSubmit={(values) => handleAdd(values).then(() => {})}
        visible={createModalOpen}
        requestColumns={requestColumns}
        responseColumns={responseColumns}
      />
    </PageContainer>
  );
};

export default TableList;
