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
import type { ActionType, ProColumns, ProDescriptionsItemProps } from '@ant-design/pro-components';
import { PageContainer, ProDescriptions, ProTable } from '@ant-design/pro-components';
import '@umijs/max';
import { Button, Drawer, message, Popconfirm } from 'antd';
import React, { useRef, useState } from 'react';

const TableList: React.FC = () => {
    /**
     * @en-US Pop-up window of new window
     * @zh-CN 新建窗口的弹窗
     *  */
    const [createModalOpen, handleModalOpen] = useState<boolean>(false);
    /**
     * @en-US The pop-up window of the distribution update window
     * @zh-CN 分布更新窗口的弹窗
     * */
    const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);
    const [showModalOpen, handleShowModalOpen] = useState<boolean>(false);

    const [showDetail, setShowDetail] = useState<boolean>(false);
    const actionRef = useRef<ActionType>();
    const [currentRow, setCurrentRow] = useState<API.InterfaceInfoVO>();

    /**
     * @en-US Add node
     * @zh-CN 添加节点
     * @param fields
     */
    const handleAdd = async (fields: API.InterfaceInfoVO) => {
        const hide = message.loading('adding');
        try {
            await addInterfaceInfoUsingPost({
                ...fields,
            });
            hide();
            message.success('Creation Successful');
            handleModalOpen(false);
            actionRef.current?.reload();
            return true;
        } catch (error: any) {
            hide();
            message.error('Creation Failed，' + error.message);
            return false;
        }
    };

    /**
     * @en-US Update node
     * @zh-CN 更新节点
     *
     * @param fields
     */
    const handleUpdate = async (fields: API.InterfaceInfoVO) => {
        if (!currentRow) {
            return;
        }
        const hide = message.loading('In Progress');
        try {
            await updateInterfaceInfoUsingPost({
                id: currentRow.id,
                ...fields,
            });
            hide();
            message.success('Operation Successful');
            return true;
        } catch (error: any) {
            hide();
            message.error('Operation Failed' + error.message);
            return false;
        }
    };

    /**
     *  Delete node
     * @zh-CN 删除节点
     *
     * @param record
     */
    const handleRemove = async (record: API.InterfaceInfoVO) => {
        const hide = message.loading('In Progress');
        if (!record) return true;
        try {
            await deleteInterfaceInfoUsingPost({
                id: record.id,
            });
            hide();
            message.success('Operation Successful');
            actionRef.current?.reload();
            return true;
        } catch (error) {
            hide();
            message.error('Operation Failed');
            return false;
        }
    };

    /**
     *  发布接口
     *
     * @param record
     */
    const handleOnline = async (record: API.InterfaceInfoInvokeRequest) => {
        const hide = message.loading('Publishing');
        if (!record) return true;
        try {
            const res = await onlineInterfaceInfoUsingPost({
                host: record.host,
                id: record.id,
                method: record.method,
                requestParams: record.requestParams,
            });
            if (res.code === 0) {
                message.success('Publishing Successful');
            }
            hide();
            actionRef.current?.reload();
            return true;
        } catch (error) {
            hide();
            message.error('Publishing Failed');
            return false;
        }
    };

  /**
   * Offline the interface
   *
   * @param record
   */
  const handleOffline = async (record: API.IdRequest) => {
    const hide = message.loading('Offlining');
    if (!record) return true;
    try {
      await offlineInterfaceInfoUsingPost({
        id: record.id,
      });
      hide();
      message.success('Offline successful');
      actionRef.current?.reload();
      return true;
    } catch (error) {
      hide();
      message.error('Offline failed');
      return false;
    }
  };

  /**
   * Columns displayed in the table
   */
  const columns: ProColumns<API.InterfaceInfoVO>[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      valueType: 'index',
    },
    {
      title: 'Interface Name',
      dataIndex: 'name',
      valueType: 'text',
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Description',
      dataIndex: 'description',
      valueType: 'textarea',
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Request Method',
      dataIndex: 'method',
      valueType: 'text',
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Host Name',
      dataIndex: 'host',
      valueType: 'text',
      hideInTable: true,
      hideInSearch: true,
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Interface Address',
      dataIndex: 'url',
      valueType: 'text',
      hideInTable: true,
      hideInSearch: true,
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Request Parameters',
      dataIndex: 'requestParams',
      valueType: 'jsonCode',
      hideInTable: true,
      hideInSearch: true,
      formItemProps: {
        rules: [
          {
            required: true,
          },
        ],
      },
    },
    {
      title: 'Request Header',
      dataIndex: 'requestHeader',
      valueType: 'jsonCode',
      hideInTable: true,
      hideInSearch: true,
    },
    {
      title: 'Response Header',
      dataIndex: 'responseHeader',
      valueType: 'jsonCode',
      hideInTable: true,
      hideInSearch: true,
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
          text: 'Open',
          status: 'Processing',
        },
      },
    },
    {
      title: 'Creation Time',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInForm: true,
    },
    {
      title: 'Update Time',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInForm: true,
      hideInTable: true,
      hideInSearch: true,
    },
    {
      title: 'Actions',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => {
        return record.status === 0
          ? [
            <Button
              key="detail"
              onClick={() => {
                handleShowModalOpen(true);
                setCurrentRow(record);
              }}
            >
              Detail
            </Button>,
            <Button
              key="update"
              onClick={() => {
                handleUpdateModalOpen(true);
                setCurrentRow(record);
              }}
            >
              Edit
            </Button>,
            <Button
              key="online"
              onClick={() => {
                handleOnline(record);
              }}
            >
              Publish
            </Button>,
            <Button
              danger
              key="remove"
              onClick={() => {
                handleRemove(record);
              }}
            >
              Delete
            </Button>,
          ]
          : [
            <Button
              key="detail"
              onClick={() => {
                handleShowModalOpen(true);
                setCurrentRow(record);
              }}
            >
              Detail
            </Button>,
            <Button
              key="update"
              onClick={() => {
                handleUpdateModalOpen(true);
                setCurrentRow(record);
              }}
            >
              Edit
            </Button>,
            <Button
              key="offline"
              onClick={() => {
                handleOffline(record);
              }}
            >
              Offline
            </Button>,
            <Popconfirm
              title="Delete data"
              key="remove"
              description="Are you sure you want to delete this data?"
              icon={<QuestionCircleOutlined style={{ color: 'red' }} />}
              onConfirm={() => {
                handleRemove(record);
              }}
            >
              <Button danger>Delete</Button>
            </Popconfirm>,
          ];
      },
    },
  ];

  const requestColumns: ProColumns<API.RequestParamsRemarkVO>[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      width: '15%',
    },
    {
      title: 'Required',
      key: 'isRequired',
      dataIndex: 'isRequired',
      valueType: 'select',
      valueEnum: {
        yes: {
          text: 'Yes',
        },
        no: {
          text: 'No',
        },
      },
      width: '15%',
    },
    {
      title: 'Type',
      dataIndex: 'type',
      width: '15%',
    },
    {
      title: 'Description',
      dataIndex: 'remark',
    },
    {
      title: 'Actions',
      valueType: 'option',
      width: '10%',
      render: () => {
        return null;
      },
    },
  ];

  const responseColumns: ProColumns<API.RequestParamsRemarkVO>[] = [
    {
      title: 'Name',
      dataIndex: 'name',
      width: '15%',
    },
    {
      title: 'Type',
      dataIndex: 'type',
      width: '15%',
    },
    {
      title: 'Description',
      dataIndex: 'remark',
    },
    {
      title: 'Actions',
      valueType: 'option',
      width: '10%',
      render: () => {
        return null;
      },
    },
  ];

  return (
    <PageContainer>
      <ProTable<API.InterfaceInfoVO, API.PageParams>
        headerTitle={'Query Table'}
        actionRef={actionRef}
        rowKey="key"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              handleModalOpen(true);
            }}
          >
            <PlusOutlined /> Create New
          </Button>,
        ]}
                request={async (params) => {
                    console.log('---------->', params);
                    const res = await listInterfaceInfoVoByPageUsingPost({
                        ...params,
                    });
                    if (res?.data) {
                        return {
                            data: res?.data.records ?? [],
                            success: true,
                            total: res.data.total ?? 0,
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
            />

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
                setVisible={handleUpdateModalOpen}
                visible={updateModalOpen}
                values={currentRow ?? {}}
                requestColumns={requestColumns}
                responseColumns={responseColumns}
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
                onSubmit={(values) => {
                    return handleAdd(values).then((r) => {});
                }}
                visible={createModalOpen}
                requestColumns={requestColumns}
                responseColumns={responseColumns}
            />
        </PageContainer>
    );
};
export default TableList;
