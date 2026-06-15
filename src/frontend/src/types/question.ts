export interface ContentBlock {
  type: 'text' | 'image'
  content: string
}

export interface Question {
  id: number
  createdBy: string
  orderIndex: number
  questionBlocks: ContentBlock[]
  options: string[]
  answer: string | null
  expectPhoto: boolean
  isBriefing: boolean
  expectsTextInput: boolean
  briefingPrimaryButtonText: string | null
  showBriefingPrimaryButton: boolean
  briefingSecondaryButtonText: string | null
  showBriefingSecondaryButton: boolean
  afterAnswerButtonText: string | null
  showAfterAnswerButton: boolean
  hintBlocks: ContentBlock[]
  explanationBlocks: ContentBlock[]
  mark: number | null
  createdAt: string
  updatedAt: string
  updatedBy: string | null
  folderId: number | null
}

export interface QuestionRequest {
  questionBlocks?: ContentBlock[]
  orderIndex?: number
  options?: string[]
  answer?: string | null
  expectPhoto?: boolean
  isBriefing?: boolean
  expectsTextInput?: boolean
  briefingPrimaryButtonText?: string | null
  showBriefingPrimaryButton?: boolean
  briefingSecondaryButtonText?: string | null
  showBriefingSecondaryButton?: boolean
  afterAnswerButtonText?: string | null
  showAfterAnswerButton?: boolean
  hintBlocks?: ContentBlock[]
  explanationBlocks?: ContentBlock[]
  mark?: number | null
  folderId?: number | null
}

export interface ExportedQuestion {
  questionBlocks: ContentBlock[]
  options: string[]
  answer: string | null
  expectPhoto: boolean
  isBriefing: boolean
  expectsTextInput: boolean
  briefingPrimaryButtonText: string | null
  showBriefingPrimaryButton: boolean
  briefingSecondaryButtonText: string | null
  showBriefingSecondaryButton: boolean
  afterAnswerButtonText: string | null
  showAfterAnswerButton: boolean
  hintBlocks: ContentBlock[]
  explanationBlocks: ContentBlock[]
  mark: number | null
}

export interface ExportData {
  version: string
  exportedAt: string
  exportedBy: string
  sourceApp: string
  questions: ExportedQuestion[]
}

export interface ImportResult {
  imported: number
  errors: string[]
}

