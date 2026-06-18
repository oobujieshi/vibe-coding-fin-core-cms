-- ============================================
-- 财务管理模块 - 数据库初始化脚本
-- 此目录下的 .sql 文件会在容器首次启动时自动执行
-- ============================================

-- 创建数据库（如果 docker-compose 未创建）
CREATE DATABASE IF NOT EXISTS fincore
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE fincore;

-- 后续建表语句将在此追加
