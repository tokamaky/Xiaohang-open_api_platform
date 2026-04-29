import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Typography, Card, Image, Button } from 'antd';
import { MailOutlined, InfoCircleOutlined, PictureOutlined, SmileOutlined } from '@ant-design/icons';
import logicImg from '../../../public/Open_Api_Plateform_UML.png';
import './index.less';

const { Content } = Layout;
const { Title, Paragraph } = Typography;

const PleaseRead: React.FC = () => (
  <PageContainer>
    <Layout style={{ padding: '24px', background: 'transparent' }}>
      <Content style={{ maxWidth: '820px', margin: '0 auto' }} className="pleaseread-page">
        <Card className="pr-card pr-card-main" bodyStyle={{ padding: '36px' }}>
          <Title level={2} className="pr-main-title">
            Welcome to API Marketplace Platform
          </Title>

          {/* Notice card */}
          <Card className="pr-card pr-notice-card" bordered={false}>
            <div className="pr-card-header">
              <InfoCircleOutlined className="pr-card-icon pr-icon-info" />
              <Title level={4} className="pr-card-title pr-title-info">Why Things Might Not Update Instantly</Title>
            </div>
            <Paragraph className="pr-paragraph">
              Due to the high deployment fees, I have opted for a slower server. This means:
            </Paragraph>
            <div className="pr-list">
              <div className="pr-list-item">
                <span className="pr-list-num">1</span>
                <p>
                  The <strong>Remaining Invocations</strong> and <strong>Invocation Count</strong> may not update immediately.
                  They will be correct the next time you open the app. Thank you for your patience!
                </p>
              </div>
              <div className="pr-list-item">
                <span className="pr-list-num">2</span>
                <p>
                  The first time you click <strong>Invoke</strong> in the <strong>Online Invocation</strong> section,
                  you might encounter an error due to server cold-start. Just try again!
                </p>
              </div>
            </div>
          </Card>

          {/* Logic diagram card */}
          <Card className="pr-card pr-diagram-card" bordered={false}>
            <div className="pr-card-header">
              <PictureOutlined className="pr-card-icon pr-icon-success" />
              <Title level={4} className="pr-card-title pr-title-success">Logic Diagram</Title>
            </div>
            <Paragraph className="pr-paragraph">
              A visual representation of the system architecture and data flow.
            </Paragraph>
            <Image
              src={logicImg}
              alt="Logic Diagram"
              preview
              className="pr-diagram"
            />
          </Card>

          {/* Feedback card */}
          <Card className="pr-card pr-feedback-card" bordered={false}>
            <div className="pr-card-header">
              <SmileOutlined className="pr-card-icon pr-icon-warning" />
              <Title level={4} className="pr-card-title pr-title-warning">Feedback & Suggestions</Title>
            </div>
            <Paragraph className="pr-paragraph">
              Always looking to improve! If you have ideas or encounter issues, reach out:
            </Paragraph>
            <Button
              type="link"
              href="mailto:jxh186045@gmail.com"
              icon={<MailOutlined />}
              className="pr-email-link"
            >
              jxh186045@gmail.com
            </Button>
          </Card>

          <p className="pr-footer">Thank you for your understanding and support!</p>
        </Card>
      </Content>
    </Layout>
  </PageContainer>
);

export default PleaseRead;
