import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Typography, Card, List, Tag, Button } from 'antd';
import {
  CloudOutlined, DatabaseOutlined, FileOutlined, DeploymentUnitOutlined,
  ExclamationCircleOutlined, CheckCircleOutlined, WarningOutlined,
} from '@ant-design/icons';
import './index.less';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const tasks = [
  {
    section: 'Database',
    icon: <DatabaseOutlined className="task-icon" />,
    color: 'var(--primary)',
    items: [
      { description: 'Add Redis data structure server', importance: 'high' },
    ],
  },
  {
    section: 'Backend',
    icon: <CloudOutlined className="task-icon" />,
    color: 'var(--primary)',
    items: [
      { description: 'Create file controller for updating user profile pics', importance: 'medium' },
      { description: 'Use cloud services to store files', importance: 'low' },
    ],
  },
  {
    section: 'Frontend',
    icon: <FileOutlined className="task-icon" />,
    color: 'var(--primary)',
    items: [
      { description: 'Create an "Update Profile" button', importance: 'high' },
    ],
  },
  {
    section: 'Deployment',
    icon: <DeploymentUnitOutlined className="task-icon" />,
    color: 'var(--primary)',
    items: [
      { description: 'Faster deployment infrastructure', importance: 'low' },
    ],
  },
];

const getImportanceIcon = (importance: string) => {
  switch (importance) {
    case 'high': return <ExclamationCircleOutlined />;
    case 'medium': return <WarningOutlined />;
    case 'low': return <CheckCircleOutlined />;
    default: return null;
  }
};

const getPriorityLabel = (importance: string) => {
  const map: Record<string, string> = { high: 'High', medium: 'Medium', low: 'Low' };
  return map[importance] || importance;
};

const Todo: React.FC = () => (
  <PageContainer>
    <Layout style={{ padding: '24px', background: 'transparent' }}>
      <Content style={{ maxWidth: '820px', margin: '0 auto' }} className="todo-page">
        <Card className="todo-card-main" bodyStyle={{ padding: '36px' }}>
          <div className="todo-header">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
            </svg>
            <div>
              <Title level={2} className="todo-main-title">Roadmap &amp; Todos</Title>
              <Paragraph className="todo-subtitle">Planned improvements for this platform. Follow along or suggest new ideas.</Paragraph>
            </div>
          </div>

          {tasks.map((section, idx) => (
            <div key={idx} className="todo-section">
              <div className="todo-section-header">
                {section.icon}
                <span className="todo-section-title">{section.section}</span>
                <span className="todo-section-count">{section.items.length} item{section.items.length !== 1 ? 's' : ''}</span>
              </div>

              <List
                itemLayout="horizontal"
                dataSource={section.items}
                renderItem={(item) => (
                  <List.Item className="todo-list-item">
                    <div className="todo-item-content">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" opacity="0.4">
                        <circle cx="12" cy="12" r="10" />
                      </svg>
                      <span className="todo-item-desc">{item.description}</span>
                    </div>
                    <Tag className={`todo-priority-tag priority-${item.importance}`}>
                      {getImportanceIcon(item.importance)}
                      <span>{getPriorityLabel(item.importance)}</span>
                    </Tag>
                  </List.Item>
                )}
              />
            </div>
          ))}

          <div className="todo-footer">
            <p>Want to suggest a new feature?</p>
            <Button
              type="link"
              href="mailto:jxh186045@gmail.com"
              icon={<CloudOutlined />}
              className="todo-email-link"
            >
              jxh186045@gmail.com
            </Button>
          </div>
        </Card>
      </Content>
    </Layout>
  </PageContainer>
);

export default Todo;
