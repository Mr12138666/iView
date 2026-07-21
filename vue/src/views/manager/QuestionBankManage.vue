<template>
  <div class="question-admin">
    <el-tabs v-model="data.activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="题库" name="banks">
        <div class="toolbar">
          <el-input v-model="data.bankQuery.title" clearable prefix-icon="Search" placeholder="题库名称" />
          <el-select v-model="data.bankQuery.interviewType" clearable placeholder="面试类型">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="data.bankQuery.difficulty" clearable placeholder="难度">
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="data.bankQuery.jobDirection" clearable placeholder="岗位方向" />
          <el-button type="info" plain @click="loadBanks">查询</el-button>
          <el-button type="warning" plain @click="resetBanks">重置</el-button>
        </div>

        <div class="actions">
          <el-button type="primary" plain @click="openBankForm()">新增题库</el-button>
          <el-button type="danger" plain @click="deleteBatch('/questionBank', data.bankSelections, loadBanks)">批量删除</el-button>
        </div>

        <el-table stripe :data="data.banks" @selection-change="rows => data.bankSelections = rows.map(item => item.id)">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="题库名称" min-width="160" />
          <el-table-column prop="interviewType" label="类型" width="110">
            <template #default="{ row }">{{ typeLabel(row.interviewType) }}</template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="{ row }">{{ difficultyLabel(row.difficulty) }}</template>
          </el-table-column>
          <el-table-column prop="jobDirection" label="岗位方向" min-width="140" />
          <el-table-column prop="tags" label="标签" min-width="140" show-overflow-tooltip />
          <el-table-column prop="enabled" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" circle :icon="Edit" @click="openBankForm(row)" />
              <el-button type="danger" circle :icon="Delete" @click="deleteOne('/questionBank', row.id, loadBanks)" />
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="data.bankTotal"
          v-model:current-page="data.bankPageNum"
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          :total="data.bankTotal"
          @current-change="loadBanks"
        />
      </el-tab-pane>

      <el-tab-pane label="题目" name="questions">
        <div class="toolbar">
          <el-input v-model="data.questionQuery.title" clearable prefix-icon="Search" placeholder="题目标题" />
          <el-select v-model="data.questionQuery.interviewType" clearable placeholder="面试类型">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-select v-model="data.questionQuery.difficulty" clearable placeholder="难度">
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="data.questionQuery.tags" clearable placeholder="标签" />
          <el-button type="info" plain @click="loadQuestions">查询</el-button>
          <el-button type="warning" plain @click="resetQuestions">重置</el-button>
        </div>

        <div class="actions">
          <el-button type="primary" plain @click="openQuestionForm()">新增题目</el-button>
          <el-button type="danger" plain @click="deleteBatch('/interviewQuestion', data.questionSelections, loadQuestions)">批量删除</el-button>
        </div>

        <el-table stripe :data="data.questions" @selection-change="rows => data.questionSelections = rows.map(item => item.id)">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="题目标题" min-width="170" />
          <el-table-column prop="content" label="题干" min-width="260" show-overflow-tooltip />
          <el-table-column prop="interviewType" label="类型" width="110">
            <template #default="{ row }">{{ typeLabel(row.interviewType) }}</template>
          </el-table-column>
          <el-table-column prop="difficulty" label="难度" width="100">
            <template #default="{ row }">{{ difficultyLabel(row.difficulty) }}</template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" circle :icon="Edit" @click="openQuestionForm(row)" />
              <el-button type="danger" circle :icon="Delete" @click="deleteOne('/interviewQuestion', row.id, loadQuestions)" />
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="data.questionTotal"
          v-model:current-page="data.questionPageNum"
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          :total="data.questionTotal"
          @current-change="loadQuestions"
        />
      </el-tab-pane>

      <el-tab-pane label="题库组题" name="relations">
        <div class="toolbar">
          <el-select v-model="data.relationQuery.questionBankId" clearable filterable placeholder="题库">
            <el-option v-for="item in data.bankOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
          <el-select v-model="data.relationQuery.questionId" clearable filterable placeholder="题目">
            <el-option v-for="item in data.questionOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
          <el-button type="info" plain @click="loadRelations">查询</el-button>
          <el-button type="warning" plain @click="resetRelations">重置</el-button>
        </div>

        <div class="actions">
          <el-button type="primary" plain @click="openRelationForm()">添加题目</el-button>
          <el-button type="danger" plain @click="deleteBatch('/questionBankQuestion', data.relationSelections, loadRelations)">批量删除</el-button>
        </div>

        <el-table stripe :data="data.relations" @selection-change="rows => data.relationSelections = rows.map(item => item.id)">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="bankTitle" label="题库" min-width="190" />
          <el-table-column prop="questionTitle" label="题目" min-width="220" />
          <el-table-column prop="sortOrder" label="排序" width="90" />
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" circle :icon="Edit" @click="openRelationForm(row)" />
              <el-button type="danger" circle :icon="Delete" @click="deleteOne('/questionBankQuestion', row.id, loadRelations)" />
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="data.relationTotal"
          v-model:current-page="data.relationPageNum"
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          :total="data.relationTotal"
          @current-change="loadRelations"
        />
      </el-tab-pane>

      <el-tab-pane label="评分规则" name="rules">
        <div class="toolbar">
          <el-select v-model="data.ruleQuery.interviewType" clearable placeholder="面试类型">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="data.ruleQuery.dimension" clearable prefix-icon="Search" placeholder="评分维度" />
          <el-button type="info" plain @click="loadRules">查询</el-button>
          <el-button type="warning" plain @click="resetRules">重置</el-button>
        </div>

        <div class="actions">
          <el-button type="primary" plain @click="openRuleForm()">新增规则</el-button>
          <el-button type="danger" plain @click="deleteBatch('/scoringRule', data.ruleSelections, loadRules)">批量删除</el-button>
        </div>

        <el-table stripe :data="data.rules" @selection-change="rows => data.ruleSelections = rows.map(item => item.id)">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="interviewType" label="类型" width="110">
            <template #default="{ row }">{{ typeLabel(row.interviewType) }}</template>
          </el-table-column>
          <el-table-column prop="dimension" label="评分维度" min-width="160" />
          <el-table-column prop="criteria" label="评分标准" min-width="300" show-overflow-tooltip />
          <el-table-column prop="weight" label="权重" width="100" />
          <el-table-column prop="enabled" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.enabled === 1 ? 'success' : 'info'">{{ row.enabled === 1 ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" circle :icon="Edit" @click="openRuleForm(row)" />
              <el-button type="danger" circle :icon="Delete" @click="deleteOne('/scoringRule', row.id, loadRules)" />
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="data.ruleTotal"
          v-model:current-page="data.rulePageNum"
          background
          layout="prev, pager, next"
          :page-size="data.pageSize"
          :total="data.ruleTotal"
          @current-change="loadRules"
        />
      </el-tab-pane>
    </el-tabs>

    <el-dialog title="题库信息" v-model="data.bankVisible" width="52%" destroy-on-close>
      <el-form :model="data.bankForm" label-width="90px">
        <el-form-item label="题库名称"><el-input v-model="data.bankForm.title" /></el-form-item>
        <el-form-item label="面试类型">
          <el-select v-model="data.bankForm.interviewType" placeholder="请选择">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="data.bankForm.difficulty" placeholder="请选择">
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位方向"><el-input v-model="data.bankForm.jobDirection" placeholder="如 Java 后端、前端开发、产品经理" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="data.bankForm.tags" placeholder="多个标签用逗号分隔" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="data.bankForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="data.bankForm.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.bankVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBank">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="题目信息" v-model="data.questionVisible" width="58%" destroy-on-close>
      <el-form :model="data.questionForm" label-width="100px">
        <el-form-item label="题目标题"><el-input v-model="data.questionForm.title" /></el-form-item>
        <el-form-item label="面试类型">
          <el-select v-model="data.questionForm.interviewType" placeholder="请选择">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="data.questionForm.difficulty" placeholder="请选择">
            <el-option v-for="item in difficultyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签"><el-input v-model="data.questionForm.tags" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="data.questionForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="题干"><el-input v-model="data.questionForm.content" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="参考答案"><el-input v-model="data.questionForm.referenceAnswer" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="追问要点"><el-input v-model="data.questionForm.followUpPoints" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="评分维度"><el-input v-model="data.questionForm.scoringDimensions" type="textarea" :rows="3" placeholder="如 技术深度、表达结构、工程经验" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.questionVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="题库组题" v-model="data.relationVisible" width="46%" destroy-on-close>
      <el-form :model="data.relationForm" label-width="90px">
        <el-form-item label="题库">
          <el-select v-model="data.relationForm.questionBankId" filterable placeholder="请选择题库">
            <el-option v-for="item in data.bankOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目">
          <el-select v-model="data.relationForm.questionId" filterable placeholder="请选择题目">
            <el-option v-for="item in data.questionOptions" :key="item.id" :label="item.title" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="data.relationForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.relationVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRelation">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog title="评分规则" v-model="data.ruleVisible" width="52%" destroy-on-close>
      <el-form :model="data.ruleForm" label-width="90px">
        <el-form-item label="面试类型">
          <el-select v-model="data.ruleForm.interviewType" placeholder="请选择">
            <el-option v-for="item in interviewTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分维度"><el-input v-model="data.ruleForm.dimension" /></el-form-item>
        <el-form-item label="权重"><el-input-number v-model="data.ruleForm.weight" :min="0" :max="1" :step="0.05" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="data.ruleForm.enabled" :active-value="1" :inactive-value="0" /></el-form-item>
        <el-form-item label="评分标准"><el-input v-model="data.ruleForm.criteria" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.ruleVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const interviewTypeOptions = [
  { label: '技术面试', value: 'TECHNICAL' },
  { label: 'HR面试', value: 'BEHAVIORAL' },
  { label: '综合面试', value: 'COMPREHENSIVE' },
  { label: '管理面试', value: 'LEADERSHIP' }
]

const difficultyOptions = [
  { label: '初级', value: 'JUNIOR' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'SENIOR' }
]

const data = reactive({
  activeTab: 'banks',
  pageSize: 10,

  banks: [],
  bankOptions: [],
  bankSelections: [],
  bankQuery: {},
  bankForm: {},
  bankVisible: false,
  bankPageNum: 1,
  bankTotal: 0,

  questions: [],
  questionOptions: [],
  questionSelections: [],
  questionQuery: {},
  questionForm: {},
  questionVisible: false,
  questionPageNum: 1,
  questionTotal: 0,

  relations: [],
  relationSelections: [],
  relationQuery: {},
  relationForm: {},
  relationVisible: false,
  relationPageNum: 1,
  relationTotal: 0,

  rules: [],
  ruleSelections: [],
  ruleQuery: {},
  ruleForm: {},
  ruleVisible: false,
  rulePageNum: 1,
  ruleTotal: 0
})

const typeLabel = value => interviewTypeOptions.find(item => item.value === value)?.label || value || '-'
const difficultyLabel = value => difficultyOptions.find(item => item.value === value)?.label || value || '-'

const clone = row => JSON.parse(JSON.stringify(row || {}))

const assertRequired = (form, fields) => {
  for (const [key, label] of fields) {
    if (form[key] === undefined || form[key] === null || form[key] === '') {
      ElMessage.warning(`请填写${label}`)
      return false
    }
  }
  return true
}

const requestSave = (baseUrl, form, onSuccess) => {
  const action = form.id ? request.put(`${baseUrl}/update`, form) : request.post(`${baseUrl}/add`, form)
  action.then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      onSuccess()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const deleteOne = (baseUrl, id, reload) => {
  ElMessageBox.confirm('删除后数据无法恢复，确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete(`${baseUrl}/delete/${id}`).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        reload()
        loadOptions()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const deleteBatch = (baseUrl, ids, reload) => {
  if (!ids.length) {
    ElMessage.warning('请选择数据')
    return
  }
  ElMessageBox.confirm('删除后数据无法恢复，确定删除吗？', '删除确认', { type: 'warning' }).then(() => {
    request.delete(`${baseUrl}/delete/batch`, { data: ids }).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        reload()
        loadOptions()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const loadBanks = () => {
  request.get('/questionBank/selectPage', {
    params: { ...data.bankQuery, pageNum: data.bankPageNum, pageSize: data.pageSize }
  }).then(res => {
    if (res.code === '200') {
      data.banks = res.data?.list || []
      data.bankTotal = res.data?.total || 0
    }
  })
}

const loadQuestions = () => {
  request.get('/interviewQuestion/selectPage', {
    params: { ...data.questionQuery, pageNum: data.questionPageNum, pageSize: data.pageSize }
  }).then(res => {
    if (res.code === '200') {
      data.questions = res.data?.list || []
      data.questionTotal = res.data?.total || 0
    }
  })
}

const loadRelations = () => {
  request.get('/questionBankQuestion/selectPage', {
    params: { ...data.relationQuery, pageNum: data.relationPageNum, pageSize: data.pageSize }
  }).then(res => {
    if (res.code === '200') {
      data.relations = res.data?.list || []
      data.relationTotal = res.data?.total || 0
    }
  })
}

const loadRules = () => {
  request.get('/scoringRule/selectPage', {
    params: { ...data.ruleQuery, pageNum: data.rulePageNum, pageSize: data.pageSize }
  }).then(res => {
    if (res.code === '200') {
      data.rules = res.data?.list || []
      data.ruleTotal = res.data?.total || 0
    }
  })
}

const loadOptions = () => {
  request.get('/questionBank/selectAll').then(res => {
    if (res.code === '200') data.bankOptions = res.data || []
  })
  request.get('/interviewQuestion/selectAll').then(res => {
    if (res.code === '200') data.questionOptions = res.data || []
  })
}

const resetBanks = () => {
  data.bankQuery = {}
  data.bankPageNum = 1
  loadBanks()
}

const resetQuestions = () => {
  data.questionQuery = {}
  data.questionPageNum = 1
  loadQuestions()
}

const resetRelations = () => {
  data.relationQuery = {}
  data.relationPageNum = 1
  loadRelations()
}

const resetRules = () => {
  data.ruleQuery = {}
  data.rulePageNum = 1
  loadRules()
}

const openBankForm = row => {
  data.bankForm = row ? clone(row) : { enabled: 1, interviewType: 'TECHNICAL', difficulty: 'INTERMEDIATE' }
  data.bankVisible = true
}

const saveBank = () => {
  if (!assertRequired(data.bankForm, [['title', '题库名称'], ['interviewType', '面试类型'], ['difficulty', '难度']])) return
  requestSave('/questionBank', data.bankForm, () => {
    data.bankVisible = false
    loadBanks()
    loadOptions()
  })
}

const openQuestionForm = row => {
  data.questionForm = row ? clone(row) : { enabled: 1, interviewType: 'TECHNICAL', difficulty: 'INTERMEDIATE' }
  data.questionVisible = true
}

const saveQuestion = () => {
  if (!assertRequired(data.questionForm, [['title', '题目标题'], ['content', '题干'], ['interviewType', '面试类型'], ['difficulty', '难度']])) return
  requestSave('/interviewQuestion', data.questionForm, () => {
    data.questionVisible = false
    loadQuestions()
    loadOptions()
  })
}

const openRelationForm = row => {
  data.relationForm = row ? clone(row) : { sortOrder: 0 }
  data.relationVisible = true
}

const saveRelation = () => {
  if (!assertRequired(data.relationForm, [['questionBankId', '题库'], ['questionId', '题目']])) return
  requestSave('/questionBankQuestion', data.relationForm, () => {
    data.relationVisible = false
    loadRelations()
  })
}

const openRuleForm = row => {
  data.ruleForm = row ? clone(row) : { enabled: 1, interviewType: 'TECHNICAL', weight: 0.25 }
  data.ruleVisible = true
}

const saveRule = () => {
  if (!assertRequired(data.ruleForm, [['interviewType', '面试类型'], ['dimension', '评分维度'], ['criteria', '评分标准'], ['weight', '权重']])) return
  requestSave('/scoringRule', data.ruleForm, () => {
    data.ruleVisible = false
    loadRules()
  })
}

const handleTabChange = name => {
  if (name === 'banks') loadBanks()
  if (name === 'questions') loadQuestions()
  if (name === 'relations') {
    loadOptions()
    loadRelations()
  }
  if (name === 'rules') loadRules()
}

loadBanks()
loadOptions()
</script>

<style scoped>
.question-admin {
  min-height: calc(100vh - 124px);
  background:
      linear-gradient(rgba(14, 165, 233, 0.05) 1px, transparent 1px),
      linear-gradient(90deg, rgba(14, 165, 233, 0.04) 1px, transparent 1px),
      linear-gradient(145deg, #f8fbff 0%, #eef7fb 46%, #f7fbff 100%);
  background-size: 34px 34px, 34px 34px, auto;
  border: 1px solid rgba(14, 165, 233, 0.16);
  border-radius: 8px;
  padding: 18px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 18px 52px rgba(15, 23, 42, 0.08);
}

.toolbar,
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.toolbar {
  padding: 14px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 8px;
  backdrop-filter: blur(14px);
}

.actions {
  align-items: center;
}

.toolbar :deep(.el-input),
.toolbar :deep(.el-select) {
  width: 190px;
}

.question-admin :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(14, 165, 233, 0.16);
}

.question-admin :deep(.el-tabs__item) {
  color: #52667a;
  font-weight: 600;
  letter-spacing: 0;
}

.question-admin :deep(.el-tabs__item.is-active) {
  color: #0891b2;
}

.question-admin :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #0891b2, #10b981);
}

.question-admin :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(14, 165, 233, 0.12);
  --el-table-header-bg-color: #edf8fb;
  --el-table-row-hover-bg-color: #f0fbff;
}

.question-admin :deep(.el-table th.el-table__cell) {
  color: #254156;
  font-weight: 700;
}

.question-admin :deep(.el-button) {
  border-radius: 8px;
}

.question-admin :deep(.el-button--primary.is-plain) {
  --el-button-bg-color: #ecfeff;
  --el-button-border-color: #67e8f9;
  --el-button-text-color: #0e7490;
}

.question-admin :deep(.el-button--info.is-plain) {
  --el-button-bg-color: #eef6ff;
  --el-button-border-color: #93c5fd;
  --el-button-text-color: #1d4ed8;
}

.question-admin :deep(.el-dialog) {
  border-radius: 8px;
}

.question-admin :deep(.el-pagination) {
  margin-top: 12px;
}
</style>
