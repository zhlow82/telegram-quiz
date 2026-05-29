export interface Question {
  id: number
  orderIndex: number
  questionText: string
  intro: string | null
  introBlue: boolean
  questionImagePaths: string[]
  options: string[]
  answer: string | null
  expectPhoto: boolean
  isBriefing: boolean
  hintText: string | null
  hintImagePaths: string[]
  explanationTexts: string[]
  explanationImagePaths: string[]
  createdAt: string
  updatedAt: string
}

export interface QuestionRequest {
  questionText: string
  orderIndex?: number
  intro?: string | null
  introBlue?: boolean
  questionImagePaths?: string[]
  options?: string[]
  answer?: string | null
  expectPhoto?: boolean
  isBriefing?: boolean
  hintText?: string | null
  hintImagePaths?: string[]
  explanationTexts?: string[]
  explanationImagePaths?: string[]
}
