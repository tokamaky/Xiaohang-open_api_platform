import {history} from '@umijs/max';
import {Button} from 'antd';
import React from 'react';
import './404.less';

const NoFoundPage: React.FC = () => (
  <div className="not-found-page">
    <div className="not-found-content">
      {/* Animated SVG */}
      <div className="not-found-icon">
        <svg width="120" height="120" viewBox="0 0 120 120" fill="none">
          <circle cx="60" cy="60" r="55" stroke="rgba(0,212,170,0.15)" strokeWidth="2" />
          <circle cx="60" cy="60" r="38" stroke="rgba(0,212,170,0.1)" strokeWidth="1" strokeDasharray="4 4" />
          <circle cx="60" cy="60" r="6" fill="#00D4AA" opacity="0.6">
            <animate attributeName="r" values="6;10;6" dur="2s" repeatCount="indefinite" />
            <animate attributeName="opacity" values="0.6;0.2;0.6" dur="2s" repeatCount="indefinite" />
          </circle>
          <line x1="60" y1="52" x2="60" y2="48" stroke="#00D4AA" strokeWidth="3" strokeLinecap="round" />
          <line x1="60" y1="72" x2="60" y2="68" stroke="rgba(0,212,170,0.4)" strokeWidth="2" strokeLinecap="round" />
          <text x="60" y="57" textAnchor="middle" fill="rgba(0,212,170,0.8)" fontSize="20" fontWeight="700" fontFamily="monospace">?</text>
        </svg>
      </div>

      <div className="not-found-text">
        <h1 className="not-found-code">404</h1>
        <p className="not-found-title">Lost in the API</p>
        <p className="not-found-desc">
          The endpoint you are looking for does not exist or has been moved.
          <br />Check the URL and try again.
        </p>
      </div>

      <Button
        type="primary"
        className="not-found-btn"
        onClick={() => history.push('/')}
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" /><polyline points="9 22 9 12 15 12 15 22" />
        </svg>
        Back to Home
      </Button>

      <div className="not-found-terminal">
        <div className="terminal-bar">
          <span /><span /><span />
          <span className="terminal-title-bar">404 — Not Found</span>
        </div>
        <div className="terminal-body-notfound">
          <p><span className="t-dim">$</span> <span className="t-cmd">GET</span> <span className="t-path">/this/endpoint/does/not/exist</span></p>
          <p className="t-err">{"{ error: '404 Not Found', message: 'Resource not found on this server' }"}</p>
        </div>
      </div>
    </div>
  </div>
);

export default NoFoundPage;
