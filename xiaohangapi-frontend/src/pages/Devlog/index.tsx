import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { Layout, Card, Typography, Tag, Collapse, Timeline, Divider } from 'antd';
import {
  SafetyCertificateOutlined,
  ThunderboltOutlined,
  RobotOutlined,
  RocketOutlined,
  ToolOutlined,
  ScheduleOutlined,
  DatabaseOutlined,
  CloudOutlined,
  ApiOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import './index.less';

const { Content } = Layout;
const { Title, Text, Paragraph } = Typography;
const { Panel } = Collapse;

interface FeatureItem {
  title: string;
  description: string;
  tag?: string;
  tagColor?: string;
}

interface Phase {
  key: string;
  phase: string;
  date: string;
  title: string;
  subtitle: string;
  icon: React.ReactNode;
  color: string;
  features: FeatureItem[];
  learnings: string[];
}

const phases: Phase[] = [
  {
    key: 'phase-1',
    phase: 'Phase 1',
    date: '2026/04/15',
    title: 'Distributed Rate Limiting',
    subtitle: 'Gateway layer security & traffic protection',
    icon: <SafetyCertificateOutlined />,
    color: '#00D4AA',
    features: [
      {
        title: 'Three-Dimensional Rate Limiting',
        description:
          'Implemented sliding window rate limiting across three dimensions: global (platform-wide), per-user (by accessKey), and per-interface (by path). Each dimension serves a distinct purpose — global limits protect the platform from traffic spikes, per-user limits prevent resource monopolization, and per-interface limits protect individual APIs from being overwhelmed.',
        tag: 'Redis + Lua',
        tagColor: '#FF6B6B',
      },
      {
        title: 'Sliding Window Algorithm with Redis ZSET',
        description:
          'Used Redis sorted sets (ZSET) where each request is stored as a member with its timestamp as the score. The sliding window removes expired entries and counts remaining entries atomically via a Lua script. This approach is more accurate than fixed-window counters and handles burst traffic gracefully without the "boundary reset" problem.',
        tag: 'Redis ZSET',
        tagColor: '#4ECDC4',
      },
      {
        title: 'Atomic Lua Script Execution',
        description:
          'Implemented the sliding window logic as a Redis Lua script that runs atomically. This guarantees accuracy under high concurrency — no race conditions between reading the count and incrementing it. The script removes stale entries, counts current requests in the window, and adds a new entry only if under the limit, all in a single atomic operation.',
        tag: 'Lua Script',
        tagColor: '#45B7D1',
      },
      {
        title: 'Fail-Open Architecture',
        description:
          'When Redis is unavailable (connection timeout or error), the rate limiter returns true and allows the request through. This ensures the platform remains available even when the rate limiting subsystem fails, following the "fail open" principle for non-critical security features. The trade-off is that during Redis outages, rate limiting is temporarily bypassed.',
        tag: 'Resilience',
        tagColor: '#F0B429',
      },
      {
        title: 'HTTP 429 Response with Retry-After Header',
        description:
          'When rate limit is triggered, the Gateway returns HTTP 429 Too Many Requests with a JSON body containing error code 42900 and a descriptive message. A Retry-After: 60 header tells the client how many seconds to wait before retrying. The GlobalFilterExceptionHandler was enhanced to map error codes to appropriate HTTP status codes.',
        tag: 'HTTP 429',
        tagColor: '#96CEB4',
      },
      {
        title: 'Three-Tier Gateway Filter Chain',
        description:
          'Integrate rate limiting into the existing Gateway filter chain at the correct position: after signature authentication (to know the user) but before routing to the interface service. This ensures we have the accessKey available for per-user limiting and the path for per-interface limiting, while avoiding unnecessary checks for invalid requests.',
        tag: 'Spring Cloud Gateway',
        tagColor: '#9B59B6',
      },
    ],
    learnings: [
      'Redis is not just a cache — its data structures (STRING, HASH, ZSET, LIST, SET) enable sophisticated distributed algorithms. The ZSET score-based sliding window is far superior to INCR + EXPIRE for rate limiting.',
      'Lua scripts in Redis are evaluated atomically as a single operation. This eliminates the need for distributed locks when implementing counters. Every rate limiting implementation should use Lua scripts for correctness under concurrency.',
      '"Fail-open" vs "fail-closed" is a critical design decision. For security features like rate limiting, failing open keeps the service available but temporarily unprotected. For payment systems, failing closed is better. Always make this decision consciously.',
      'The sliding window algorithm solves the boundary reset problem of fixed-window counters. At 12:00:59, a fixed window would reset, allowing a burst of 2x the rate. Sliding windows smoothly transition and prevent this.',
      'Three dimensions of rate limiting (global/user/interface) give fine-grained control. A single global limit is too coarse — one abusive user could block all other users. Per-user + per-interface provides fairness and protection.',
    ],
  },
];

const Changelog: React.FC = () => {
  return (
    <PageContainer>
      <Layout style={{ padding: '24px', background: 'transparent' }}>
        <Content style={{ maxWidth: '900px', margin: '0 auto' }} className="changelog-page">
          {/* Page Header */}
          <Card className="cl-header-card" bodyStyle={{ padding: '32px 36px' }}>
            <div className="cl-header">
              <div className="cl-header-icon">
                <HistoryOutlined />
              </div>
              <div className="cl-header-text">
                <Title level={2} className="cl-main-title">
                  Developer Devlog
                </Title>
                <Paragraph className="cl-subtitle">
                  Tracking the evolution of this platform — new features, technical deep-dives, and
                  lessons learned along the way.
                </Paragraph>
              </div>
            </div>
            <Divider className="cl-header-divider" />
            <div className="cl-header-stats">
              <div className="cl-stat">
                <span className="cl-stat-number">1</span>
                <span className="cl-stat-label">Phase Completed</span>
              </div>
              <div className="cl-stat-sep" />
              <div className="cl-stat">
                <span className="cl-stat-number">6</span>
                <span className="cl-stat-label">Features Added</span>
              </div>
              <div className="cl-stat-sep" />
              <div className="cl-stat">
                <span className="cl-stat-number">5</span>
                <span className="cl-stat-label">Key Learnings</span>
              </div>
            </div>
          </Card>

          {/* Phase Cards */}
          <div className="cl-phases-container">
            {phases.map((phase, index) => (
              <Card
                key={phase.key}
                className={`cl-phase-card cl-phase-${index + 1}`}
                bodyStyle={{ padding: '0' }}
              >
                {/* Phase Header */}
                <div className="cl-phase-header" style={{ borderLeftColor: phase.color }}>
                  <div className="cl-phase-meta">
                    <Tag
                      className="cl-phase-tag"
                      style={{ background: `${phase.color}15`, borderColor: `${phase.color}40`, color: phase.color }}
                    >
                      {phase.phase}
                    </Tag>
                    <span className="cl-phase-date">
                      <ScheduleOutlined /> {phase.date}
                    </span>
                  </div>
                  <div className="cl-phase-title-row">
                    <span className="cl-phase-icon" style={{ color: phase.color }}>
                      {phase.icon}
                    </span>
                    <Title level={3} className="cl-phase-title">
                      {phase.title}
                    </Title>
                  </div>
                  <Paragraph className="cl-phase-subtitle">{phase.subtitle}</Paragraph>
                </div>

                {/* Features Accordion */}
                <div className="cl-phase-body">
                  <Collapse
                    bordered={false}
                    defaultActiveKey={['feature-0']}
                    expandIconPosition="end"
                    className="cl-features-collapse"
                  >
                    {phase.features.map((feature, fIdx) => (
                      <Panel
                        key={`feature-${fIdx}`}
                        header={
                          <div className="cl-feature-header">
                            <span className="cl-feature-num">{String(fIdx + 1).padStart(2, '0')}</span>
                            <span className="cl-feature-title">{feature.title}</span>
                            {feature.tag && (
                              <Tag className="cl-feature-tag" style={{ background: `${feature.tagColor}15`, borderColor: `${feature.tagColor}30`, color: feature.tagColor }}>
                                {feature.tag}
                              </Tag>
                            )}
                          </div>
                        }
                      >
                        <Paragraph className="cl-feature-desc">{feature.description}</Paragraph>
                      </Panel>
                    ))}
                  </Collapse>
                </div>

                {/* Learnings Section */}
                <div className="cl-learnings-section">
                  <div className="cl-learnings-header">
                    <RocketOutlined className="cl-learnings-icon" />
                    <span>Technical Insights & Learnings</span>
                  </div>
                  <Timeline
                    className="cl-learnings-timeline"
                    items={phase.learnings.map((learning, lIdx) => ({
                      key: lIdx,
                      dot: (
                        <div className="cl-learning-dot" style={{ borderColor: phase.color }}>
                          {lIdx + 1}
                        </div>
                      ),
                      children: (
                        <Paragraph className="cl-learning-text">{learning}</Paragraph>
                      ),
                    }))}
                  />
                </div>
              </Card>
            ))}
          </div>

          {/* Future Phases Placeholder */}
          <Card className="cl-future-card" bodyStyle={{ padding: '32px' }}>
            <div className="cl-future-header">
              <div className="cl-future-icon">
                <ScheduleOutlined />
              </div>
              <Title level={4} className="cl-future-title">
                More Phases Coming
              </Title>
            </div>
            <Paragraph className="cl-future-desc">
              Phases 2, 3, 4, and 5 are planned. Each will follow the same structure: a
              problem statement, implementation details, and real technical insights gained
              from building.
            </Paragraph>
            <div className="cl-future-list">
              <div className="cl-future-item">
                <span className="cl-future-item-num">02</span>
                <span className="cl-future-item-name">Circuit Breaker & Fallback</span>
                <Tag color="default">Planned</Tag>
              </div>
              <div className="cl-future-item">
                <span className="cl-future-item-num">03</span>
                <span className="cl-future-item-name">SkyWalking Distributed Tracing</span>
                <Tag color="default">Planned</Tag>
              </div>
              <div className="cl-future-item">
                <span className="cl-future-item-num">04</span>
                <span className="cl-future-item-name">GitHub Actions CI/CD Pipeline</span>
                <Tag color="default">Planned</Tag>
              </div>
              <div className="cl-future-item">
                <span className="cl-future-item-num">05</span>
                <span className="cl-future-item-name">RabbitMQ Async Processing</span>
                <Tag color="default">Planned</Tag>
              </div>
            </div>
          </Card>
        </Content>
      </Layout>
    </PageContainer>
  );
};

export default Changelog;
