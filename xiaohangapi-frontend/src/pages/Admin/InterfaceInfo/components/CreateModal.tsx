import { DrawerForm, ProColumns, ProFormInstance, ProFormText } from '@ant-design/pro-components';
import { EditableProTable } from '@ant-design/pro-table';
import '@umijs/max';
import { Form, Input } from 'antd';
import React, { useRef, useState } from 'react';

export type FormValueType = {
    target?: string;
    template?: string;
    type?: string;
    time?: string;
    frequency?: string;
} & Partial<API.RuleListItem>;

export type Props = {
    columns: ProColumns<API.InterfaceInfoVO>[];
    setVisible: (visible: boolean) => void;
    onSubmit: (values: API.InterfaceInfoVO) => Promise<void>;
    visible: boolean;
    requestColumns: ProColumns<API.RequestParamsRemarkVO>[];
    responseColumns: ProColumns<API.RequestParamsRemarkVO>[];
};

const CreateModal: React.FC<Props> = (props) => {
    const { visible, setVisible, onSubmit, requestColumns, responseColumns } = props;
    const formRef = useRef<ProFormInstance>();
    // @ts-ignore
    const [requestEditableKeys, setRequestEditableKeys] = useState<React.Key[]>(() => []);
    const [requestDataSource, setRequestDataSource] = useState<
        readonly API.RequestParamsRemarkVO[]
    >([]);
    // @ts-ignore
    const [responseEditableKeys, setResponseEditableKeys] = useState<React.Key[]>(() => []);
    const [responseDataSource, setResponseDataSource] = useState<
        readonly API.ResponseParamsRemarkVO[]
    >([]);
    return (
      <DrawerForm<API.InterfaceInfoVO>
        onFinish={async (value) => {
          console.log('---------->', value);
          onSubmit?.(value);
        }}
        formRef={formRef}
        formKey="update-modal-form"
        autoFocusFirstInput
        onOpenChange={setVisible}
        title="Add New Interface"
        open={visible}
      >
        <ProFormText
          name="name"
          label="Interface Name"
          rules={[{ required: true, message: 'Interface name cannot be empty!' }]}
        />
        <ProFormText
          name="description"
          label="Description"
          rules={[{ required: true, message: 'Description cannot be empty!' }]}
        />
        <ProFormText
          name="method"
          label="Request Method"
          rules={[{ required: true, message: 'Request method cannot be empty!' }]}
        />
        <ProFormText
          name="host"
          label="Hostname"
          rules={[{ required: true, message: 'Hostname cannot be empty!' }]}
        />
        <ProFormText
          name="url"
          label="Interface URL"
          rules={[{ required: true, message: 'Interface URL cannot be empty!' }]}
        />
        <Form.Item name="requestParams" label="Request Parameters Example">
          <Input.TextArea />
        </Form.Item>
        <Form.Item name="requestParamsRemark" label="Request Parameters Description">
          <EditableProTable<API.RequestParamsRemarkVO>
            rowKey="id"
            toolBarRender={false}
            columns={requestColumns}
            value={requestDataSource}
            onChange={setRequestDataSource}
            recordCreatorProps={{
              newRecordType: 'dataSource',
              position: 'bottom',
              record: () => ({
                id: Date.now(),
                isRequired: 'no',
                type: 'string',
              }),
            }}
            editable={{
              type: 'multiple',
              editableKeys: requestEditableKeys,
              onChange: setRequestEditableKeys,
              actionRender: (row, _, dom) => {
                return [dom.delete];
              },
              onValuesChange: (record, recordList) => {
                setRequestDataSource(recordList);
                formRef.current?.setFieldsValue({
                  requestParamsRemark: recordList,
                });
              },
            }}
          />
        </Form.Item>
        <Form.Item name="responseParamsRemark" label="Response Parameters Description">
          <EditableProTable<API.ResponseParamsRemarkVO>
            rowKey="id"
            toolBarRender={false}
            columns={responseColumns}
            value={responseDataSource}
            onChange={setResponseDataSource}
            recordCreatorProps={{
              newRecordType: 'dataSource',
              position: 'bottom',
              record: () => ({
                id: Date.now(),
                type: 'string',
              }),
            }}
            editable={{
              type: 'multiple',
              editableKeys: responseEditableKeys,
              onChange: setResponseEditableKeys,
              actionRender: (row, _, dom) => {
                return [dom.delete];
              },
              onValuesChange: (record, recordList) => {
                setResponseDataSource(recordList);
                formRef.current?.setFieldsValue({
                  responseParamsRemark: recordList,
                });
              },
            }}
          />
        </Form.Item>
        <Form.Item name="requestHeader" label="Request Header">
          <Input.TextArea />
        </Form.Item>
        <Form.Item name="responseHeader" label="Response Header">
          <Input.TextArea />
        </Form.Item>
      </DrawerForm>
    );
};
export default CreateModal;
