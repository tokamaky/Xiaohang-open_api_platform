
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns, ProDescriptionsItemProps } from '@ant-design/pro-components';
import {
  FooterToolbar,
  PageContainer,
  ProDescriptions,
  ProTable,
} from '@ant-design/pro-components';

import { Button, Drawer, message } from 'antd';
import React, { useRef, useState } from 'react';

import UpdateModal from './components/UpdateModal';
import {
  addInterfaceInfoUsingPost,
  deleteInterfaceInfoUsingPost,
  listInterfaceInfoByPageUsingGet,
  offlineInterfaceInfoUsingPost,
  onlineInterfaceInfoUsingPost,
  updateInterfaceInfoUsingPost,
} from "@/services/xiaohangapi-backend/interfaceInfoController";
import CreateModal from "@/pages/Admin/InterfaceInfo/components/CreateModal";
import {SortOrder} from "antd/es/table/interface";

const TableList: React.FC = () => {
  /**
   * @en-US Pop-up window of new window
   * @zh-CN 新建窗口的弹窗
   *  */
  const [createModalVisible, handleModalVisible] = useState<boolean>(false);
  /**
   * @en-US The pop-up window of the distribution update window
   * @zh-CN 分布更新窗口的弹窗
   * */
  const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);
  const [showDetail, setShowDetail] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.InterfaceInfo>();
  const [selectedRowsState, setSelectedRows] = useState<API.InterfaceInfo[]>([]);

  /**
   * @en-US Add node
   * @zh-CN 添加节点
   * @param fields
   */
  const handleAdd = async (fields: API.InterfaceInfo) => {
    const hide = message.loading('Adding');
    try {
      await addInterfaceInfoUsingPost({
        ...fields,
      });
      hide();
      message.success('Created successfully.');
      handleModalVisible(false);
      return true;
    } catch (error: any) {
      hide();
      message.error('Created failed，' + error.message);
      return false;
    }
  };

  /**
   * @en-US Update node
   * @zh-CN 更新节点
   *
   * @param fields
   */
  const handleUpdate = async (fields:  API.InterfaceInfo) => {
    if (!currentRow) {
      return;
    }
    const hide = message.loading('In progress');
    try {
      await updateInterfaceInfoUsingPost({
        id: currentRow.id,
        ...fields,
      });
      hide();
      message.success('Operation successful');
      return true;
    } catch (error: any) {
      hide();
      message.error('Operation failed，' + error.message);
      return false;
    }
  };

  /**
   * 发布接口
   *
   * @param record
   */
  const handleOnlineInterface = async (record: API.IdRequest) => {
    const hide = message.loading('Publishing');
    if (!record) return true;
    try {
      await onlineInterfaceInfoUsingPost({
        id: record.id
      });
      hide();
      message.success('Operation successful');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('Operation failed，' + error.message);
      return false;
    }
  };

  /**
   * 下线接口
   *
   * @param record
   */
  const handleOfflineInterface = async (record: API.IdRequest) => {
    const hide = message.loading('Publishing');
    if (!record) return true;
    try {
      await offlineInterfaceInfoUsingPost({
        id: record.id
      });
      hide();
      message.success('Operation successful');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('Operation failed，' + error.message);
      return false;
    }
  };

  /**
   *  Delete node
   * @zh-CN 删除节点
   *
   * @param record
   */
  const handleRemoveInterfaceInfo = async (record: API.InterfaceInfo) => {
    const hide = message.loading('正在删除');
    if (!record) return true;
    try {
      await deleteInterfaceInfoUsingPost({
        id: record.id,
      });
      hide();
      message.success('删除成功');
      actionRef.current?.reload();
      return true;
    } catch (error: any) {
      hide();
      message.error('删除失败，' + error.message);
      return false;
    }
  };

  /**
   * @en-US International configuration
   * @zh-CN 国际化配置
   * */

  const columns: ProColumns<API.InterfaceInfo>[] = [
    {
      title: 'Interface Name',
      dataIndex: 'name',
      valueType: 'text',
      formItemProps: {
        rules: [{
          required: true,
        }]
      }
    },
    {
      title: 'Description',
      dataIndex: 'description',
      valueType: 'textarea',
    },
    {
      title: 'Request Method',
      dataIndex: 'method',
      valueType: 'text',
    },
    {
      title: 'url',
      dataIndex: 'url',
      valueType: 'text',
    },
    {
      title: 'Request Params',
      dataIndex: 'requestParams',
      valueType: 'jsonCode',
    },
    {
      title: 'Request Header',
      dataIndex: 'requestHeader',
      valueType: 'jsonCode',
    },
    {
      title: 'Response Header',
      dataIndex: 'responseHeader',
      valueType: 'jsonCode',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      hideInForm: true,
      valueEnum: {
        0: {
          text: 'Closed',
          status: 'Default',
        },
        1: {
          text: 'Started',
          status: 'Processing',
        },
      },
    },
    {
      title: 'Operation',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => [
        <Button
          key="config"
          type={"link"}
          onClick={() => {
            handleUpdateModalOpen(true);
            setCurrentRow(record);
          }}
        >
          Modify
        </Button>,
        record.status === 0 ? (
          <Button
            key="online"
            type={'link'}
            onClick={() => {
              handleOnlineInterface(record);
            }}
          >
            Online
          </Button>
        ) : (
          <Button
            key="offline"
            type={'text'}
            // danger={true}
            onClick={() => {
              handleOfflineInterface(record);
            }}
          >
            Offline
          </Button>
        ),
        <Button
          key="delete"
          type={'text'}
          danger={true}
          onClick={() => {
            handleRemoveInterfaceInfo(record);
          }}
        >
          delete
        </Button>,
      ],
    },
  ];

  return (
    <PageContainer>
      <ProTable<API.RuleListItem, API.PageParams>
        headerTitle={'Query Table'}
        actionRef={actionRef}
        //tableLayout ="fixed"
        scroll={{ x: 'max-content' }}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              handleModalVisible(true);
            }}
          >
            <PlusOutlined /> Create New
          </Button>,
        ]}
        request={async (
          params,
          sort: Record<string, SortOrder>,
          filter: Record<string, React.ReactText[] | null>,
        ) => {
          const res: any = await listInterfaceInfoByPageUsingGet({
            ...params,
          });
          if (res?.data) {
            return {
              data: res?.data.records || [],
              success: true,
              total: res?.data.total || 0,
            };
          } else {
            return {
              data: [],
              success: false,
              total: 0,
            };
          }
        }}
        columns={columns}
        rowSelection={{
          onChange: (_, selectedRows) => {
            setSelectedRows(selectedRows);
          },
        }}
      />
      {selectedRowsState?.length > 0 && (
        <FooterToolbar
          extra={
            <div>
              <a
                style={{
                  fontWeight: 600,
                }}
              >
                {selectedRowsState.length}
              </a>{' '}
              items selected &nbsp;&nbsp;
            </div>
          }
        >
          <Button
            onClick={async () => {
              await handleRemoveInterfaceInfo(selectedRowsState);
              setSelectedRows([]);
              actionRef.current?.reloadAndRest?.();
            }}
          >
            批量删除
          </Button>
          <Button type="primary">批量审批</Button>
        </FooterToolbar>
      )}
      <UpdateModal
        columns={columns}
        onSubmit={async (value) => {
          const success = await handleUpdate(value);
          if (success) {
            handleUpdateModalOpen(false);
            setCurrentRow(undefined);
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}
        onCancel={() => {
          handleUpdateModalOpen(false);
          if (!showDetail) {
            setCurrentRow(undefined);
          }
        }}
        visible={updateModalOpen}
        values={currentRow || {}}
      />

      <Drawer
        width={600}
        open={showDetail}
        onClose={() => {
          setCurrentRow(undefined);
          setShowDetail(false);
        }}
        closable={false}
      >
        {currentRow?.name && (
          <ProDescriptions<API.RuleListItem>
            column={2}
            title={currentRow?.name}
            request={async () => ({
              data: currentRow || {},
            })}
            params={{
              id: currentRow?.name,
            }}
            columns={columns as ProDescriptionsItemProps<API.RuleListItem>[]}
          />
        )}
      </Drawer>
      <CreateModal
        columns={columns}
        onCancel={() => { handleModalVisible(false)}}
        onSubmit={(values) => handleAdd(values)}
        open={createModalVisible}
      />
    </PageContainer>
  );
};
export default TableList;
