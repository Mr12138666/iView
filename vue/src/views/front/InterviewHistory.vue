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
          <h3>多模态材料</h3>
          <div v-loading="multimodalLoading" class="multimodal-panel">
            <div v-if="multimodalRecords.length" class="multimodal-list">
              <article
                  v-for="record in multimodalRecords"
                  :key="record.id"
                  class="multimodal-item"
              >
                <div class="multimodal-meta">
                  <el-tag :type="modalityTagType(record.modality)" effect="dark">
                    {{ modalityLabel(record.modality) }}
                  </el-tag>
                  <span>#{{ record.id }}</span>
                  <span>{{ record.status || 'RESERVED' }}</span>
                </div>
                <p>{{ recordSummary(record) }}</p>
                <pre v-if="record.codeContent" class="code-preview">{{ previewCode(record.codeContent) }}</pre>
                <small v-if="record.suggestion">{{ record.suggestion }}</small>
              </article>
            </div>
            <el-empty v-else description="暂无多模态材料" />
          </div>
        </section>

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
            <div v-if="questionReviews.length" class="question-review-list">
              <article
                  v-for="(review, index) in questionReviews"
                  :key="review.questionId || index"
                  class="question-review-card"
              >
                <div class="question-review-head">
                  <span>Q{{ index + 1 }}</span>
                  <strong>{{ review.questionTitle || `题目 ${review.questionId || index + 1}` }}</strong>
                  <em>{{ normalizeScore(review.score) }} 分</em>
                </div>
                <p v-if="review.coverage"><b>覆盖情况：</b>{{ review.coverage }}</p>
                <p v-if="review.deductionReason"><b>扣分原因：</b>{{ review.deductionReason }}</p>
                <p v-if="review.suggestion"><b>训练建议：</b>{{ review.suggestion }}</p>
              </article>
            </div>
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
const multimodalLoading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const multimodalRecords = ref([])
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

const questionReviews = computed(() => parseQuestionReviews(detail.value?.report?.questionReviews))

const parseQuestionReviews = (value) => {
  if (!value) {
    return []
  }
  if (Array.isArray(value)) {
    return value
  }
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch (error) {
    return []
  }
}

const normalizeScore = (score) => {
  const value = Number(score)
  return Number.isFinite(value) ? Math.max(0, Math.min(100, value)).toFixed(0) : '--'
}

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

const modalityLabel = (modality) => ({
  AUDIO: '语音',
  VIDEO: '视频',
  CODE: '代码'
}[modality] || '材料')

const modalityTagType = (modality) => ({
  AUDIO: 'success',
  VIDEO: 'warning',
  CODE: 'primary'
}[modality] || 'info')

const recordSummary = (record) => {
  if (record.modality === 'CODE') {
    return `语言：${record.codeLanguage || 'unknown'}`
  }
  if (record.modality === 'AUDIO') {
    return formatPendingMarker(record.audioUrl, '音频材料已记录')
  }
  if (record.modality === 'VIDEO') {
    return formatPendingMarker(record.videoUrl, '视频材料已记录')
  }
  return '材料已记录'
}

const formatPendingMarker = (value, fallback) => {
  if (!value) {
    return fallback
  }
  return value.replace('pending://', '待处理：')
}

const previewCode = (code) => {
  if (!code) {
    return ''
  }
  return code.length > 260 ? `${code.slice(0, 260)}...` : code
}

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

const loadMultimodalRecords = async (sessionId) => {
  multimodalLoading.value = true
  try {
    const response = await request.get(`/api/multimodal/session/${sessionId}`)
    if (response.code !== '200') {
      throw new Error(response.msg || '多模态材料加载失败')
    }
    multimodalRecords.value = response.data || []
  } catch (error) {
    multimodalRecords.value = []
    ElMessage.warning(error.message || '多模态材料加载失败')
  } finally {
    multimodalLoading.value = false
  }
}

const openDetail = async (id) => {
  try {
    detail.value = null
    multimodalRecords.value = []
    const response = await request.get(`/api/interview/session/${id}`)
    if (response.code !== '200') {
      throw new Error(response.msg || '面试详情加载失败')
    }
    detail.value = response.data
    detailVisible.value = true
    loadMultimodalRecords(id)
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

.multimodal-panel {
  min-height: 108px;
}

.multimodal-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.multimodal-item {
  padding: 14px;
  border: 1px solid rgba(64, 158, 255, .18);
  border-radius: 8px;
  background:
      linear-gradient(135deg, rgba(240, 249, 255, .96), rgba(246, 255, 252, .92));
}

.multimodal-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}

.multimodal-item p {
  margin: 10px 0 0;
  color: #223044;
  line-height: 1.6;
}

.multimodal-item small {
  display: block;
  margin-top: 8px;
  color: #64748b;
  line-height: 1.6;
}

.code-preview {
  max-height: 136px;
  margin: 10px 0 0;
  padding: 12px;
  overflow: auto;
  border-radius: 6px;
  color: #d8fbff;
  background: #071625;
  font-size: 12px;
  line-height: 1.55;
  white-space: pre-wrap;
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

.question-review-list {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.question-review-card {
  padding: 14px 16px;
  border: 1px solid rgba(64, 158, 255, .18);
  border-radius: 8px;
  background: linear-gradient(135deg, #f8fbff, #f3fff9);
}

.question-review-head {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.question-review-head span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 28px;
  border-radius: 6px;
  color: #071625;
  background: #d6ff65;
  font-size: 12px;
  font-weight: 800;
}

.question-review-head strong {
  color: #162235;
  font-size: 15px;
}

.question-review-head em {
  color: #0f766e;
  font-style: normal;
  font-weight: 800;
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
