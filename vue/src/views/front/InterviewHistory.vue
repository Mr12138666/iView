<template>
  <div class="interview-history-page">
    <section class="history-hero">
      <div>
        <div class="eyebrow">Interview telemetry</div>
        <h2>面试记录</h2>
        <p>查看历史面试、对话记录和 AI 评估报告，追踪最近 12 次能力走势。</p>
      </div>
      <el-button type="primary" :icon="Refresh" @click="refreshAll">刷新</el-button>
    </section>

    <section class="trend-console" v-loading="trendLoading">
      <div class="metric-card">
        <span>报告数</span>
        <strong>{{ data.trend.length }}</strong>
      </div>
      <div class="metric-card">
        <span>平均分</span>
        <strong>{{ averageScore }}</strong>
      </div>
      <div class="metric-card">
        <span>最高分</span>
        <strong>{{ highestScore }}</strong>
      </div>
      <div class="trend-strip">
        <div class="trend-title">
          <el-icon><TrendCharts /></el-icon>
          <span>能力趋势</span>
        </div>
        <div v-if="data.trend.length" class="score-track">
          <div
              v-for="item in data.trend"
              :key="item.sessionId"
              class="score-node"
              :style="{ height: `${scoreHeight(item.totalScore)}%` }"
              :title="`${item.jobPosition || '模拟面试'}：${item.totalScore || 0} 分`"
          >
            <i></i>
          </div>
        </div>
        <div v-else class="empty-trend">暂无可视化报告</div>
      </div>
    </section>

    <section class="history-table-panel">
      <el-table v-loading="loading" :data="data.records" stripe>
        <el-table-column prop="jobPosition" label="目标岗位" min-width="180" />
        <el-table-column prop="interviewType" label="面试类型" width="120" />
        <el-table-column prop="difficulty" label="难度" width="100" />
        <el-table-column prop="durationMinutes" label="时长" width="90">
          <template #default="{ row }">{{ row.durationMinutes }} 分钟</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
            v-model:current-page="data.pageNum"
            v-model:page-size="data.pageSize"
            :total="data.total"
            layout="total, sizes, prev, pager, next"
            :page-sizes="[10, 20, 50]"
            @current-change="loadPage"
            @size-change="handleSizeChange"
        />
      </div>
    </section>

    <el-drawer v-model="detailVisible" title="面试详情" size="min(680px, 92vw)">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="岗位">{{ detail.session.jobPosition }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detail.session.status) }}</el-descriptions-item>
          <el-descriptions-item label="面试类型">{{ detail.session.interviewType }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ detail.session.difficulty }}</el-descriptions-item>
        </el-descriptions>

        <section class="detail-section">
          <h3>对话记录</h3>
          <div v-if="detail.messages?.length" class="message-list">
            <div v-for="message in detail.messages" :key="message.id" class="message-item">
              <span class="message-role">{{ message.role === 'assistant' ? 'AI' : message.role === 'user' ? '我' : '系统' }}</span>
              <span class="message-content">{{ message.content }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无对话记录" />
        </section>

        <section class="detail-section">
          <h3>评估报告</h3>
          <template v-if="detail.report">
            <div class="score-line">
              <span>综合评分</span>
              <strong>{{ detail.report.totalScore }} 分</strong>
            </div>
            <p><b>优势：</b>{{ detail.report.strengths }}</p>
            <p><b>待改进：</b>{{ detail.report.weaknesses }}</p>
            <p><b>改进建议：</b>{{ detail.report.suggestions }}</p>
            <p><b>下一步训练：</b>{{ detail.report.nextTraining }}</p>
          </template>
          <el-empty v-else description="报告尚未生成" />
        </section>
      </template>
      <el-empty v-else description="暂无详情" />
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, TrendCharts } from '@element-plus/icons-vue'
import request from '@/utils/request.js'

const loading = ref(false)
const trendLoading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const data = reactive({
  records: [],
  trend: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

const averageScore = computed(() => {
  if (!data.trend.length) {
    return '--'
  }
  const total = data.trend.reduce((sum, item) => sum + Number(item.totalScore || 0), 0)
  return (total / data.trend.length).toFixed(1)
})

const highestScore = computed(() => {
  if (!data.trend.length) {
    return '--'
  }
  return Math.max(...data.trend.map(item => Number(item.totalScore || 0))).toFixed(1)
})

const scoreHeight = (score) => {
  const value = Math.max(0, Math.min(100, Number(score || 0)))
  return Math.max(12, value)
}

const statusLabel = (status) => ({
  CREATED: '待开始',
  RUNNING: '进行中',
  EVALUATING: '评估中',
  FINISHED: '已完成',
  CANCELLED: '已取消'
}[status] || status)

const statusType = (status) => ({
  CREATED: 'info',
  RUNNING: 'warning',
  EVALUATING: 'warning',
  FINISHED: 'success',
  CANCELLED: 'danger'
}[status] || '')

const loadTrend = async () => {
  trendLoading.value = true
  try {
    const response = await request.get('/api/report/trend')
    if (response.code !== '200') {
      throw new Error(response.msg || '能力趋势加载失败')
    }
    data.trend = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '能力趋势加载失败')
  } finally {
    trendLoading.value = false
  }
}

const loadPage = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/interview/session/page', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize
      }
    })
    if (response.code !== '200') {
      throw new Error(response.msg || '面试记录加载失败')
    }
    data.records = response.data?.list || []
    data.total = response.data?.total || 0
  } catch (error) {
    ElMessage.error(error.message || '面试记录加载失败')
  } finally {
    loading.value = false
  }
}

const refreshAll = () => {
  loadPage()
  loadTrend()
}

const handleSizeChange = () => {
  data.pageNum = 1
  loadPage()
}

const openDetail = async (id) => {
  try {
    const response = await request.get(`/api/interview/session/${id}`)
    if (response.code !== '200') {
      throw new Error(response.msg || '面试详情加载失败')
    }
    detail.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '面试详情加载失败')
  }
}

onMounted(refreshAll)
</script>

<style scoped>
.interview-history-page {
  min-height: calc(100vh - 62px);
  padding: 30px max(18px, 6vw) 52px;
  color: #e9f7ff;
  background:
      linear-gradient(135deg, rgba(7, 22, 39, .96), rgba(12, 32, 50, .92) 52%, rgba(13, 44, 43, .92)),
      repeating-linear-gradient(90deg, rgba(93, 231, 255, .07) 0 1px, transparent 1px 88px);
}

.history-hero,
.trend-console,
.history-table-panel {
  width: min(1180px, 100%);
  margin: 0 auto;
  border: 1px solid rgba(104, 243, 255, .18);
  border-radius: 8px;
  background: rgba(6, 18, 31, .72);
  box-shadow: 0 20px 56px rgba(0, 0, 0, .24);
  backdrop-filter: blur(14px);
}

.history-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 26px 30px;
}

.eyebrow {
  margin-bottom: 10px;
  color: #68f3ff;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  text-transform: uppercase;
}

.history-hero h2 {
  margin: 0;
  color: #ffffff;
  font-size: 32px;
  font-weight: 800;
}

.history-hero p {
  margin: 10px 0 0;
  color: #a9bdca;
}

.trend-console {
  display: grid;
  grid-template-columns: repeat(3, minmax(130px, .24fr)) minmax(300px, 1fr);
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
}

.metric-card {
  min-height: 112px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, .11);
  border-radius: 8px;
  background: rgba(255, 255, 255, .06);
}

.metric-card span {
  color: #94adbc;
  font-size: 13px;
}

.metric-card strong {
  display: block;
  margin-top: 18px;
  color: #d6ff65;
  font-size: 30px;
  line-height: 1;
}

.trend-strip {
  min-height: 112px;
  padding: 16px;
  border: 1px solid rgba(104, 243, 255, .18);
  border-radius: 8px;
  background: rgba(3, 10, 20, .42);
}

.trend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #d8fbff;
  font-weight: 800;
}

.score-track {
  display: grid;
  grid-template-columns: repeat(12, minmax(10px, 1fr));
  align-items: end;
  gap: 8px;
  height: 62px;
  margin-top: 14px;
}

.score-node {
  display: flex;
  align-items: end;
  min-height: 12%;
}

.score-node i {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 8px;
  border-radius: 999px 999px 4px 4px;
  background: linear-gradient(180deg, #68f3ff, #d6ff65);
  box-shadow: 0 0 18px rgba(104, 243, 255, .22);
}

.empty-trend {
  display: flex;
  align-items: center;
  height: 62px;
  margin-top: 14px;
  color: #8da6b5;
}

.history-table-panel {
  margin-top: 16px;
  padding: 18px;
}

.history-table-panel :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-section {
  margin-top: 28px;
}

.detail-section h3 {
  margin: 0 0 14px;
  color: #1f2937;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  background: #f7f9fc;
  border-radius: 6px;
  line-height: 1.6;
}

.message-role {
  flex: 0 0 32px;
  color: #409eff;
  font-weight: 600;
}

.message-content {
  white-space: pre-wrap;
  word-break: break-word;
}

.score-line {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 14px 16px;
  margin-bottom: 16px;
  background: #f0f9ff;
  border-left: 3px solid #409eff;
}

.score-line strong {
  color: #409eff;
  font-size: 24px;
}

.detail-section p {
  line-height: 1.8;
  white-space: pre-wrap;
}

@media (max-width: 980px) {
  .trend-console {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .trend-strip {
    grid-column: 1 / -1;
  }
}

@media (max-width: 680px) {
  .interview-history-page {
    padding: 18px 12px 34px;
  }

  .history-hero {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px;
  }

  .trend-console {
    grid-template-columns: 1fr;
  }

  .history-table-panel {
    padding: 12px;
  }
}
</style>
