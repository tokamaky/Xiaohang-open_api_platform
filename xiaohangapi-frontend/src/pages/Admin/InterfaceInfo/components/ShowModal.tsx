import {
    DrawerForm,
    ProColumns,
    ProFormInstance,
    ProFormText,
    ProTable,
} from '@ant-design/pro-components';
import '@umijs/max';
import { Form, Input } from 'antd';
import React, { useEffect, useRef, useState } from 'react';

export type Props = {
    values: API.InterfaceInfoVO;
    setVisible: (visible: boolean) => void;
    visible: boolean;
    requestColumns: ProColumns<API.RequestParamsRemarkVO>[];
    responseColumns: ProColumns<API.RequestParamsRemarkVO>[];
};

const ShowModal: React.FC<Props> = (props) => {
    const { values, setVisible, visible, responseColumns, requestColumns } = props;
    const formRef = useRef<ProFormInstance>();
    const [responseDataSource, setResponseDataSource] = useState<
        readonly API.ResponseParamsRemarkVO[]
    >(() => values.responseParamsRemark || []);
    const [requestDataSource, setRequestDataSource] = useState<
        readonly API.RequestParamsRemarkVO[]
    >(() => values.requestParamsRemark || []);

    useEffect(() => {
        setRequestDataSource(values.requestParamsRemark || []);
        setResponseDataSource(values.responseParamsRemark || []);
        formRef.current?.setFieldsValue(values);
    }, [values]);
    return (
      <DrawerForm<API.InterfaceInfoVO>
        formRef={formRef}
        formKey="update-modal-form"
        autoFocusFirstInput
        onOpenChange={setVisible}
        title="View Interface"
        open={visible}
      >
        <ProFormText name="name" label="Interface Name" initialValue={values.name} disabled />
        <ProFormText
          name="description"
          label="Description"
          initialValue={values.description}
          disabled
        />
        <ProFormText name="method" label="Request Method" initialValue={values.method} disabled />
        <ProFormText name="host" label="Hostname" initialValue={values.host} disabled />
        <ProFormText name="url" label="Interface URL" initialValue={values.url} disabled />
        <Form.Item name="requestParams" label="Request Parameters Example">
          <Input.TextArea defaultValue={values.requestParams} disabled />
        </Form.Item>
        <Form.Item name="requestParamsRemark" label="Request Parameters Description">
          <ProTable<API.RequestParamsRemarkVO>
            rowKey="id"
            toolBarRender={false}
            columns={requestColumns}
            dataSource={requestDataSource}
            pagination={false}
            search={false}
          />
        </Form.Item>

        <Form.Item name="responseParamsRemark" label="Response Parameters Description">
          <ProTable<API.ResponseParamsRemarkVO>
            rowKey="id"
            toolBarRender={false}
            columns={responseColumns}
            dataSource={responseDataSource}
            pagination={false}
            search={false}
          />
        </Form.Item>
        <Form.Item name="requestHeader" label="Request Header">
          <Input.TextArea defaultValue={values.requestHeader} disabled />
        </Form.Item>
        <Form.Item name="responseHeader" label="Response Header">
          <Input.TextArea defaultValue={values.responseHeader} disabled />
        </Form.Item>
      </DrawerForm>
    );
};
export default ShowModal;
