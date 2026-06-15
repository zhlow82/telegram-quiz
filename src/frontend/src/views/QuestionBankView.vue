<template>
  <AppLayout>
    <div>

      <!-- Page header (full width, above sidebar) -->
      <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
        <div class="flex items-center gap-4">
          <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
            <BookOpen class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-2xl font-black text-slate-900 leading-tight">{{ currentFolderLabel }}</h1>
            <p class="text-sm text-slate-500 mt-0.5">
              {{ visibleQuestions.length }} question{{ visibleQuestions.length !== 1 ? 's' : '' }}
              <template v-if="typeof selectedFolderFilter === 'number' || selectedFolderFilter === 'unfiled'"> · drag to reorder</template>
            </p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg w-56">
            <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search questions..."
              class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
            />
          </div>
          <button
            class="inline-flex items-center gap-2 bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
            @click="importModalVisible = true"
          >
            <Upload class="w-4 h-4" />
            Import
          </button>
          <button
            class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
            @click="openCreate"
          >
            <Plus class="w-4 h-4" />
            Add Question
          </button>
        </div>
      </div>

      <!-- Selection action bar -->
      <div
        v-if="someSelected"
        class="flex items-center gap-3 mb-4 px-4 py-3 rounded-xl bg-blue-50 border border-blue-200"
      >
        <button
          class="w-5 h-5 rounded flex items-center justify-center text-blue-600 cursor-pointer"
          @click="toggleSelectAll"
        >
          <CheckSquare v-if="allVisibleSelected" class="w-5 h-5" />
          <Square v-else class="w-5 h-5" />
        </button>
        <span class="text-sm font-medium text-blue-900">
          {{ selectedQuestionIds.size }} of {{ visibleQuestions.length }} selected
        </span>
        <div class="flex-1" />
        <button
          class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-blue-700 bg-white border border-blue-200 hover:bg-blue-100 rounded-lg transition cursor-pointer"
          @click="exportModalVisible = true"
        >
          <Download class="w-4 h-4" />
          Export
        </button>
        <button
          class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-blue-700 bg-white border border-blue-200 hover:bg-blue-100 rounded-lg transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="duplicatingInProgress"
          @click="duplicateSelectedQuestions"
        >
          <Copy class="w-4 h-4" />
          Duplicate
        </button>
        <button
          class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-blue-700 bg-white border border-blue-200 hover:bg-blue-100 rounded-lg transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="movingInProgress"
          @click="openMoveToFolderModal"
        >
          <FolderPlus class="w-4 h-4" />
          Move to Folder
        </button>
        <button
          class="px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-white rounded-lg transition cursor-pointer"
          @click="clearSelection"
        >
          Clear
        </button>
      </div>

      <div class="flex gap-5 items-start">

      <!-- -- Folder sidebar -- -->
      <aside class="w-52 flex-shrink-0 hidden md:block">
        <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">

          <!-- All Questions -->
          <button
            class="w-full flex items-center gap-2.5 px-3.5 py-2.5 text-sm font-medium transition-colors cursor-pointer"
            :class="selectedFolderFilter === null
              ? 'bg-blue-50 text-blue-700 font-semibold'
              : 'text-slate-700 hover:bg-slate-50'"
            @click="selectedFolderFilter = null"
          >
            <Library class="w-4 h-4 flex-shrink-0" :class="selectedFolderFilter === null ? 'text-blue-600' : 'text-slate-400'" />
            <span class="flex-1 text-left truncate">All Questions</span>
            <span class="text-xs font-bold" :class="selectedFolderFilter === null ? 'text-blue-500' : 'text-slate-400'">{{ questions.length }}</span>
          </button>

          <!-- Unfiled (also a drop target) -->
          <button
            class="w-full flex items-center gap-2.5 px-3.5 py-2.5 text-sm font-medium transition-colors cursor-pointer border-t border-slate-100"
            :class="[
              selectedFolderFilter === 'unfiled' ? 'bg-blue-50 text-blue-700 font-semibold' : 'text-slate-700 hover:bg-slate-50',
              dropTarget === 'unfiled' ? 'ring-2 ring-inset ring-blue-400 bg-blue-50' : ''
            ]"
            @click="selectedFolderFilter = 'unfiled'"
            @dragover.prevent="onDragOver('unfiled')"
            @dragleave="onDragLeave"
            @drop.prevent="onDrop('unfiled')"
          >
            <Inbox class="w-4 h-4 flex-shrink-0" :class="selectedFolderFilter === 'unfiled' ? 'text-blue-600' : 'text-slate-400'" />
            <span class="flex-1 text-left truncate">Unfiled</span>
            <span class="text-xs font-bold" :class="selectedFolderFilter === 'unfiled' ? 'text-blue-500' : 'text-slate-400'">{{ unfiledCount }}</span>
          </button>

          <!-- Folders divider -->
          <div v-if="ownedFoldersModel.length || sharedFolders.length" class="border-t border-slate-100"></div>

          <!-- Owned folder list (draggable, paginated) -->
          <VueDraggable
            v-model="ownedFoldersModel"
            handle=".folder-drag-handle"
            :animation="150"
            ghost-class="drag-ghost"
            @end="persistFolderReorder"
          >
          <div v-for="folder in paginatedOwnedFolders" :key="folder.id">
            <div
              class="group flex items-center gap-2 px-3.5 py-2.5 transition-colors cursor-pointer"
              :class="[
                selectedFolderFilter === folder.id ? 'bg-blue-50 text-blue-700' : 'text-slate-700 hover:bg-slate-50',
                dropTarget === folder.id ? 'ring-2 ring-inset ring-blue-400 bg-blue-50' : ''
              ]"
              @click="selectedFolderFilter = folder.id"
              @dragover.prevent="onDragOver(folder.id)"
              @dragleave="onDragLeave"
              @drop.prevent="onDrop(folder.id)"
            >
              <!-- Rename in-place -->
              <template v-if="editingFolderId === folder.id">
                <FolderOpen class="w-4 h-4 flex-shrink-0 text-blue-500" />
                <input
                  v-model="editingFolderName"
                  class="flex-1 min-w-0 text-sm border border-blue-400 rounded px-1.5 py-0.5 outline-none bg-white text-slate-900"
                  @keydown.enter.prevent="submitFolderRename(folder.id)"
                  @keydown.escape="cancelFolderRename"
                  @blur="submitFolderRename(folder.id)"
                  ref="folderInputRef"
                />
              </template>
              <template v-else>
                <FolderIcon class="w-4 h-4 flex-shrink-0" :class="selectedFolderFilter === folder.id ? 'text-blue-500' : 'text-slate-400 group-hover:text-slate-500'" />
                <span class="flex-1 text-left truncate text-sm font-medium">{{ folder.name }}</span>
                <span class="text-xs font-bold group-hover:hidden" :class="selectedFolderFilter === folder.id ? 'text-blue-500' : 'text-slate-400'">{{ folderCounts[folder.id] ?? 0 }}</span>
                <!-- Hover actions -->
                <div class="hidden group-hover:flex items-center gap-0.5">
                  <button
                    class="folder-drag-handle w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-grab active:cursor-grabbing"
                    title="Drag to reorder"
                    @click.stop
                  ><GripVertical class="w-3 h-3" /></button>
                  <button
                    class="w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-100 transition cursor-pointer"
                    title="Share folder"
                    @click.stop="openShareModal(folder)"
                  ><Share2 class="w-3 h-3" /></button>
                  <button
                    class="w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-100 transition cursor-pointer"
                    title="Rename"
                    @click.stop="startFolderRename(folder)"
                  ><Pencil class="w-3 h-3" /></button>
                  <button
                    class="w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-red-500 hover:bg-red-50 transition cursor-pointer"
                    title="Delete folder"
                    @click.stop="confirmDeleteFolder(folder)"
                  ><Trash2 class="w-3 h-3" /></button>
                </div>
              </template>
            </div>
          </div>
          </VueDraggable>

          <!-- Folder pagination -->
          <div v-if="ownedFolderPages > 1" class="flex items-center justify-center gap-2 px-3.5 py-2 border-t border-slate-100">
            <button
              class="w-6 h-6 rounded flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition cursor-pointer disabled:opacity-30 disabled:cursor-default"
              :disabled="folderPage === 1"
              @click="folderPage--"
            >
              <ChevronLeft class="w-3.5 h-3.5" />
            </button>
            <span class="text-xs text-slate-400">{{ folderPage }} / {{ ownedFolderPages }}</span>
            <button
              class="w-6 h-6 rounded flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition cursor-pointer disabled:opacity-30 disabled:cursor-default"
              :disabled="folderPage === ownedFolderPages"
              @click="folderPage++"
            >
              <ChevronRight class="w-3.5 h-3.5" />
            </button>
          </div>

          <!-- Shared folders (static) -->
          <template v-if="sharedFolders.length">
            <div class="px-3.5 py-1.5 text-[0.65rem] font-semibold text-slate-400 uppercase tracking-wide border-t border-slate-100">Shared with me</div>
            <div v-for="folder in sharedFolders" :key="folder.id">
              <div
                class="group flex items-center gap-2 px-3.5 py-2.5 transition-colors cursor-pointer"
                :class="[
                  selectedFolderFilter === folder.id ? 'bg-blue-50 text-blue-700' : 'text-slate-700 hover:bg-slate-50',
                  dropTarget === folder.id ? 'ring-2 ring-inset ring-blue-400 bg-blue-50' : ''
                ]"
                @click="selectedFolderFilter = folder.id"
                @dragover.prevent="onDragOver(folder.id)"
                @dragleave="onDragLeave"
                @drop.prevent="onDrop(folder.id)"
              >
                <FolderSymlink class="w-4 h-4 flex-shrink-0" :class="selectedFolderFilter === folder.id ? 'text-blue-500' : 'text-slate-400 group-hover:text-slate-500'" />
                <span class="flex-1 text-left truncate text-sm font-medium">{{ folder.name }}</span>
                <span class="text-xs font-bold group-hover:hidden" :class="selectedFolderFilter === folder.id ? 'text-blue-500' : 'text-slate-400'">{{ folderCounts[folder.id] ?? 0 }}</span>
                <!-- Hover actions for shared folder -->
                <div class="hidden group-hover:flex items-center gap-0.5">
                  <button
                    class="w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-blue-600 hover:bg-blue-100 transition cursor-pointer"
                    :title="folder.role === 'CO_OWNER' ? 'Share folder' : 'View members'"
                    @click.stop="openShareModal(folder)"
                  ><Share2 class="w-3 h-3" /></button>
                  <button
                    v-if="folder.role === 'CO_OWNER'"
                    class="w-5 h-5 rounded flex items-center justify-center text-slate-400 hover:text-red-500 hover:bg-red-50 transition cursor-pointer"
                    title="Delete folder"
                    @click.stop="confirmDeleteFolder(folder)"
                  ><Trash2 class="w-3 h-3" /></button>
                </div>
              </div>
            </div>
          </template>

          <!-- Pending invitations -->
          <template v-if="pendingInvitations.length">
            <div class="border-t border-slate-100">
              <button
                class="w-full flex items-center gap-2.5 px-3.5 py-2.5 text-sm font-medium text-amber-600 hover:bg-amber-50 transition-colors cursor-pointer"
                @click="invitationsExpanded = !invitationsExpanded"
              >
                <Bell class="w-4 h-4 flex-shrink-0" />
                <span class="flex-1 text-left">Invitations</span>
                <span class="text-xs font-bold bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded-full">{{ pendingInvitations.length }}</span>
                <ChevronDown class="w-3 h-3 flex-shrink-0 transition-transform" :class="invitationsExpanded ? 'rotate-180' : ''" />
              </button>
              <div v-if="invitationsExpanded" class="px-3 pb-2 space-y-2">
                <div
                  v-for="inv in pendingInvitations"
                  :key="inv.id"
                  class="rounded-lg bg-amber-50 border border-amber-100 p-2.5"
                >
                  <p class="text-xs font-semibold text-slate-800 mb-0.5 truncate">{{ inv.folderName }}</p>
                  <p class="text-[0.65rem] text-slate-500 mb-1.5">
                    {{ inv.invitedBy }} � {{ inv.role === 'CO_OWNER' ? 'Co-owner' : 'Contributor' }}
                  </p>
                  <div class="flex gap-1.5">
                    <button
                      class="flex-1 text-[0.65rem] font-semibold bg-blue-600 hover:bg-blue-700 text-white px-2 py-1 rounded-md transition cursor-pointer"
                      @click="acceptInvite(inv.id)"
                    >Accept</button>
                    <button
                      class="flex-1 text-[0.65rem] font-semibold bg-white hover:bg-slate-100 text-slate-600 border border-slate-200 px-2 py-1 rounded-md transition cursor-pointer"
                      @click="declineInvite(inv.id)"
                    >Decline</button>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div class="border-t border-slate-100">
            <div v-if="creatingFolder" class="flex items-center gap-2 px-3 py-2">
              <FolderPlus class="w-4 h-4 text-blue-500 flex-shrink-0" />
              <input
                v-model="newFolderName"
                class="flex-1 min-w-0 text-sm border border-blue-400 rounded px-1.5 py-0.5 outline-none bg-white text-slate-900"
                placeholder="Folder name"
                @keydown.enter.prevent="submitCreateFolder"
                @keydown.escape="cancelCreateFolder"
                @blur="submitCreateFolder"
                ref="newFolderInputRef"
              />
            </div>
            <button
              v-else
              class="w-full flex items-center gap-2.5 px-3.5 py-2.5 text-sm text-slate-400 hover:text-blue-600 hover:bg-slate-50 transition-colors cursor-pointer"
              @click="startCreateFolder"
            >
              <FolderPlus class="w-4 h-4 flex-shrink-0" />
              New folder
            </button>
          </div>

        </div>
      </aside>

      <!-- -- Main content -- -->
      <div class="flex-1 min-w-0">

        <!-- Error state -->
        <div
          v-if="loadError"
          class="flex items-center gap-3 mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-200 text-red-700 text-sm"
        >
          <AlertCircle class="w-4 h-4 flex-shrink-0" />
          Failed to load questions. Please refresh.
        </div>

        <!-- Loading skeleton -->
        <div v-else-if="loading" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <div v-for="i in 4" :key="i" class="flex items-center gap-4 px-5 py-4 border-b border-slate-100 last:border-0">
            <div class="w-4 h-8 bg-slate-200 rounded animate-pulse flex-shrink-0"></div>
            <div class="flex-1 flex flex-col gap-2">
              <div class="h-3.5 bg-slate-100 rounded animate-pulse" style="animation-delay: 0.05s"></div>
              <div class="h-3 bg-slate-100 rounded animate-pulse w-2/5" style="animation-delay: 0.1s"></div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div
          v-else-if="visibleQuestions.length === 0"
          class="bg-white rounded-xl border border-slate-200 flex flex-col items-center gap-4 py-14 px-6 text-center"
        >
          <div class="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center">
            <BookOpen class="w-7 h-7 text-blue-600" />
          </div>
          <div>
            <p class="font-semibold text-slate-900 mb-1">No questions here</p>
            <p class="text-sm text-slate-500">Add your first question to get started</p>
          </div>
          <button
            class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
            @click="openCreate"
          >
            <Plus class="w-4 h-4" />
            Add Question
          </button>
        </div>

        <!-- Question list (All view � read only) -->
        <div v-else-if="selectedFolderFilter === null" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <div v-for="(q, i) in questions" :key="q.id"
            class="flex items-center gap-3 px-5 py-3.5 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors"
          >
            <button
              class="w-5 h-5 rounded flex items-center justify-center flex-shrink-0 cursor-pointer"
              :class="selectedQuestionIds.has(q.id) ? 'text-blue-600' : 'text-slate-300 hover:text-slate-400'"
              @click.stop="toggleSelect(q.id)"
            >
              <CheckSquare v-if="selectedQuestionIds.has(q.id)" class="w-5 h-5" />
              <Square v-else class="w-5 h-5" />
            </button>
            <span class="text-[0.8125rem] font-bold text-slate-300 w-[22px] shrink-0 text-right">{{ i + 1 }}</span>
            <div v-if="q.questionBlocks.some(b => b.type === 'image')" class="relative w-10 h-10 shrink-0">
              <img
                :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')?.content}`"
                class="w-10 h-10 rounded-lg object-cover border border-slate-200 bg-slate-100"
                alt=""
              />
              <span
                v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
              >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
            </div>
            <div v-else class="w-10 h-10 shrink-0" />
            <div class="flex-1 min-w-0">
              <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
              <div class="flex gap-1.5 mt-1 flex-wrap">
                <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                  <BookOpen class="w-3 h-3" />Briefing
                </span>
                <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                  <Users class="w-3 h-3" />Team Input
                </span>
                <span v-else-if="q.options.length" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-blue-100 text-blue-700">
                  <ListChecks class="w-3 h-3" />Multiple Choice
                </span>
                <span v-else-if="q.expectPhoto" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-violet-100 text-violet-700">
                  <Camera class="w-3 h-3" />Photo
                </span>
                <span v-if="!q.isBriefing && q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-slate-100 text-slate-500">{{ q.options.length }} options</span>
                <span v-if="q.folderId != null" class="inline-flex items-center gap-1 text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-emerald-50 text-emerald-700">
                  <FolderIcon class="w-3 h-3" />{{ folders.find(f => f.id === q.folderId)?.name }}
                </span>
              </div>
              <p v-if="q.updatedAt" class="text-[0.65rem] text-slate-400 mt-0.5 truncate">{{ formatRelative(q.updatedAt) }}<template v-if="q.updatedBy"> — {{ q.updatedBy }}</template></p>
            </div>
            <div class="flex items-center gap-1.5 flex-shrink-0">
              <div v-if="!q.isBriefing && !q.expectsTextInput && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
                <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
                <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
              </div>
              <div v-else class="w-9 shrink-0" />
              <button class="w-8 h-8 rounded-lg bg-slate-50 hover:bg-slate-100 flex items-center justify-center text-slate-500 transition cursor-pointer" title="Duplicate" @click="duplicateQuestion(q)">
                <Copy class="w-3.5 h-3.5" />
              </button>
              <button class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer" title="Edit" @click="openEdit(q)">
                <Pencil class="w-3.5 h-3.5" />
              </button>
              <button v-if="canDeleteQuestion(q)" class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer" title="Delete" @click="confirmDelete(q)">
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>

        <!-- Question list (Unfiled view � draggable to reorder + draggable to sidebar folders) -->
        <div v-else-if="selectedFolderFilter === 'unfiled'" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <VueDraggable
            v-model="unfiledQuestions"
            handle=".drag-handle"
            :animation="200"
            ghost-class="drag-ghost"
            @end="persistUnfiledQuestionsReorder"
          >
            <div v-for="(q, i) in unfiledQuestions" :key="q.id"
              class="flex items-center gap-3 px-5 py-3.5 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors select-none"
              :class="{ 'opacity-50': draggingQuestions.some(dq => dq.id === q.id) }"
              draggable="true"
              @dragstart="onDragStartFolder(q, $event)"
              @dragend="onDragEnd"
            >
              <button
                class="w-5 h-5 rounded flex items-center justify-center flex-shrink-0 cursor-pointer z-10"
                :class="selectedQuestionIds.has(q.id) ? 'text-blue-600' : 'text-slate-300 hover:text-slate-400'"
                @click.stop="toggleSelect(q.id)"
              >
                <CheckSquare v-if="selectedQuestionIds.has(q.id)" class="w-5 h-5" />
                <Square v-else class="w-5 h-5" />
              </button>
              <button
                class="drag-handle bg-transparent border-0 cursor-grab p-1 rounded flex shrink-0 text-slate-300 hover:text-slate-400 active:cursor-grabbing transition-colors"
                title="Drag to reorder" aria-label="Drag"
              >
                <GripVertical class="w-4 h-4" />
              </button>
              <span class="text-[0.8125rem] font-bold text-slate-300 w-[22px] shrink-0 text-right">{{ i + 1 }}</span>
              <div v-if="q.questionBlocks.some(b => b.type === 'image')" class="relative w-10 h-10 shrink-0">
                <img
                  :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')?.content}`"
                  class="w-10 h-10 rounded-lg object-cover border border-slate-200 bg-slate-100"
                  alt=""
                />
                <span
                  v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                  class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
                >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
              </div>
              <div v-else class="w-10 h-10 shrink-0" />
              <div class="flex-1 min-w-0">
                <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap">
                  <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                    <BookOpen class="w-3 h-3" />Briefing
                  </span>
                  <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                    <Users class="w-3 h-3" />Team Input
                  </span>
                  <span v-else-if="q.options.length" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-blue-100 text-blue-700">
                    <ListChecks class="w-3 h-3" />Multiple Choice
                  </span>
                  <span v-else-if="q.expectPhoto" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-violet-100 text-violet-700">
                    <Camera class="w-3 h-3" />Photo
                  </span>
                  <span v-if="!q.isBriefing && q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-slate-100 text-slate-500">{{ q.options.length }} options</span>
                </div>
                <p v-if="q.updatedAt" class="text-[0.65rem] text-slate-400 mt-0.5 truncate">{{ formatRelative(q.updatedAt) }}<template v-if="q.updatedBy"> � {{ q.updatedBy }}</template></p>
              </div>
              <div class="flex items-center gap-1.5 flex-shrink-0">
                <div v-if="!q.isBriefing && !q.expectsTextInput && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
                  <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
                  <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
                </div>
                <div v-else class="w-9 shrink-0" />
                <button class="w-8 h-8 rounded-lg bg-slate-50 hover:bg-slate-100 flex items-center justify-center text-slate-500 transition cursor-pointer" title="Duplicate" @click="duplicateQuestion(q)">
                  <Copy class="w-3.5 h-3.5" />
                </button>
                <button class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer" title="Edit" @click="openEdit(q)">
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer" title="Delete" @click="confirmDelete(q)">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </VueDraggable>
        </div>

        <!-- Question list (Folder view � draggable to reorder within folder) -->
        <div v-else class="bg-white rounded-xl border border-slate-200 overflow-hidden">
          <VueDraggable
            v-model="folderQuestions"
            handle=".drag-handle"
            :animation="200"
            ghost-class="drag-ghost"
            @end="persistFolderQuestionsReorder"
          >
            <div v-for="(q, i) in folderQuestions" :key="q.id"
              class="flex items-center gap-3 px-5 py-3.5 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors select-none"
              :class="{ 'opacity-50': draggingQuestions.some(dq => dq.id === q.id) }"
              draggable="true"
              @dragstart="onDragStartFolder(q, $event)"
              @dragend="onDragEnd"
            >
              <button
                class="w-5 h-5 rounded flex items-center justify-center flex-shrink-0 cursor-pointer z-10"
                :class="selectedQuestionIds.has(q.id) ? 'text-blue-600' : 'text-slate-300 hover:text-slate-400'"
                @click.stop="toggleSelect(q.id)"
              >
                <CheckSquare v-if="selectedQuestionIds.has(q.id)" class="w-5 h-5" />
                <Square v-else class="w-5 h-5" />
              </button>
              <button
                class="drag-handle bg-transparent border-0 cursor-grab p-1 rounded flex shrink-0 text-slate-300 hover:text-slate-400 active:cursor-grabbing transition-colors"
                title="Drag to reorder" aria-label="Drag"
              >
                <GripVertical class="w-4 h-4" />
              </button>
              <span class="text-[0.8125rem] font-bold text-slate-300 w-[22px] shrink-0 text-right">{{ i + 1 }}</span>
              <div v-if="q.questionBlocks.some(b => b.type === 'image')" class="relative w-10 h-10 shrink-0">
                <img
                  :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')?.content}`"
                  class="w-10 h-10 rounded-lg object-cover border border-slate-200 bg-slate-100"
                  alt=""
                />
                <span
                  v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                  class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
                >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
              </div>
              <div v-else class="w-10 h-10 shrink-0" />
              <div class="flex-1 min-w-0">
                <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap">
                  <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                    <BookOpen class="w-3 h-3" />Briefing
                  </span>
                  <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                    <Users class="w-3 h-3" />Team Input
                  </span>
                  <span v-else-if="q.options.length" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-blue-100 text-blue-700">
                    <ListChecks class="w-3 h-3" />Multiple Choice
                  </span>
                  <span v-else-if="q.expectPhoto" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-violet-100 text-violet-700">
                    <Camera class="w-3 h-3" />Photo
                  </span>
                  <span v-if="!q.isBriefing && q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-slate-100 text-slate-500">{{ q.options.length }} options</span>
                </div>
                <p v-if="q.updatedAt" class="text-[0.65rem] text-slate-400 mt-0.5 truncate">{{ formatRelative(q.updatedAt) }}<template v-if="q.updatedBy"> � {{ q.updatedBy }}</template></p>
              </div>
              <div class="flex items-center gap-1.5 flex-shrink-0">
                <div v-if="!q.isBriefing && !q.expectsTextInput && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
                  <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
                  <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
                </div>
                <div v-else class="w-9 shrink-0" />
                <button class="w-8 h-8 rounded-lg bg-slate-50 hover:bg-slate-100 flex items-center justify-center text-slate-500 transition cursor-pointer" title="Duplicate" @click="duplicateQuestion(q)">
                  <Copy class="w-3.5 h-3.5" />
                </button>
                <button class="w-8 h-8 rounded-lg bg-blue-50 hover:bg-blue-100 flex items-center justify-center text-blue-600 transition cursor-pointer" title="Edit" @click="openEdit(q)">
                  <Pencil class="w-3.5 h-3.5" />
                </button>
                <button v-if="canDeleteQuestion(q)" class="w-8 h-8 rounded-lg bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-500 transition cursor-pointer" title="Delete" @click="confirmDelete(q)">
                  <Trash2 class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </VueDraggable>
        </div>

      </div>
    </div>

    <!-- Form modal -->
    <QuestionFormModal
      :visible="modalVisible"
      :question="editingQuestion"
      :folders="folders"
      :default-folder-id="activeFolderId"
      @close="modalVisible = false"
      @saved="onSaved"
    />

    <!-- App dialog (confirm / alert) -->
    <AppDialog
      :visible="dialogVisible"
      :type="dialogType"
      :title="dialogTitle"
      :message="dialogMessage"
      @confirm="onDialogConfirm"
      @cancel="onDialogCancel"
    />

    <!-- Folder members / share modal -->
    <FolderMembersModal
      :visible="shareModalFolderId !== null"
      :folder-id="shareModalFolderId"
      :folder-name="shareModalFolderName"
      :folder-owner="shareModalFolderOwner"
      :current-username="currentUsername"
      :readonly="shareModalReadonly"
      @close="closeShareModal"
    />

    <!-- Export modal -->
    <ExportModal
      :visible="exportModalVisible"
      :selected-count="selectedQuestionIds.size"
      :question-ids="Array.from(selectedQuestionIds)"
      @close="exportModalVisible = false"
      @exported="clearSelection"
    />

    <!-- Import modal -->
    <ImportModal
      :visible="importModalVisible"
      @close="importModalVisible = false"
      @imported="onImported"
    />

    <!-- Move to folder modal -->
    <teleport to="body">
      <div v-if="moveToFolderModalVisible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="fixed inset-0 bg-black/40" />
        <div class="relative bg-white rounded-xl shadow-xl w-full max-w-sm mx-4 overflow-hidden">
          <div class="flex items-center justify-between px-5 py-4 border-b border-slate-200">
            <h3 class="text-lg font-bold text-slate-900">Move to Folder</h3>
            <button class="w-7 h-7 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer" @click="moveToFolderModalVisible = false">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
            </button>
          </div>
          <div class="px-5 py-4">
            <p class="text-sm text-slate-600 mb-3">Move {{ selectedQuestionIds.size }} question(s) to:</p>
            <select
              v-model="moveToFolderTarget"
              class="w-full text-sm border border-slate-200 rounded-lg px-3 py-2 outline-none focus:ring-2 focus:ring-blue-500 bg-white text-slate-700"
            >
              <option :value="null">Unfiled</option>
              <option v-for="f in folders" :key="f.id" :value="f.id">{{ f.name }}</option>
            </select>
            <div v-if="movingInProgress || duplicatingInProgress" class="mt-3">
              <div class="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                <div
                  class="h-full bg-blue-600 rounded-full transition-all duration-300"
                  :style="{ width: `${progressPercent}%` }"
                />
              </div>
              <p class="text-xs text-slate-500 mt-1.5">{{ progressLabel }}</p>
            </div>
          </div>
          <div class="flex items-center justify-end gap-2 px-5 py-3 bg-slate-50 border-t border-slate-200">
            <button
              class="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition cursor-pointer"
              @click="moveToFolderModalVisible = false"
              :disabled="movingInProgress || duplicatingInProgress"
            >
              Cancel
            </button>
            <button
              class="px-4 py-2 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
              @click="moveSelectedToFolder"
              :disabled="movingInProgress"
            >
              Move
            </button>
          </div>
        </div>
      </div>
    </teleport>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import {
  Plus, AlertCircle, BookOpen, GripVertical, Pencil, Trash2,
  Users, ListChecks, Camera,
  Library, Inbox, FolderPlus, FolderOpen, Share2, Bell, ChevronDown, ChevronLeft, ChevronRight,
  Folder as FolderIcon, FolderSymlink, Search, Copy,
  Download, Upload, CheckSquare, Square,
} from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import QuestionFormModal from '@/components/QuestionFormModal.vue'
import AppDialog from '@/components/AppDialog.vue'
import FolderMembersModal from '@/components/FolderMembersModal.vue'
import ExportModal from '@/components/ExportModal.vue'
import ImportModal from '@/components/ImportModal.vue'
import { questionsService } from '@/services/questionsService'
import { foldersService } from '@/services/foldersService'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import type { Question } from '@/types/question'
import type { Folder, FolderMember } from '@/types/folder'

const authStore = useAuthStore()
const toast = useToast()
const currentUsername = computed(() => authStore.username ?? '')

// -- Selection --------------------------------------------------------------
const selectedQuestionIds = ref<Set<number>>(new Set())

const allVisibleSelected = computed(() =>
  visibleQuestions.value.length > 0 &&
  visibleQuestions.value.every(q => selectedQuestionIds.value.has(q.id))
)

const someSelected = computed(() => selectedQuestionIds.value.size > 0)

function toggleSelectAll() {
  if (allVisibleSelected.value) {
    selectedQuestionIds.value = new Set()
  } else {
    selectedQuestionIds.value = new Set(visibleQuestions.value.map(q => q.id))
  }
}

function toggleSelect(id: number) {
  const newSet = new Set(selectedQuestionIds.value)
  if (newSet.has(id)) {
    newSet.delete(id)
  } else {
    newSet.add(id)
  }
  selectedQuestionIds.value = newSet
}

function clearSelection() {
  selectedQuestionIds.value = new Set()
}

// -- Export/Import modals ---------------------------------------------------
const exportModalVisible = ref(false)
const importModalVisible = ref(false)

// -- Mass operations --------------------------------------------------------
const duplicatingInProgress = ref(false)
const movingInProgress = ref(false)
const progressCurrent = ref(0)
const progressTotal = ref(0)
const progressLabel = ref('')
const progressPercent = computed(() => progressTotal.value > 0 ? Math.round((progressCurrent.value / progressTotal.value) * 100) : 0)

const moveToFolderModalVisible = ref(false)
const moveToFolderTarget = ref<number | null>(null)

function openMoveToFolderModal() {
  moveToFolderTarget.value = null
  moveToFolderModalVisible.value = true
}

async function duplicateSelectedQuestions() {
  const ids = Array.from(selectedQuestionIds.value)
  if (!ids.length) return
  duplicatingInProgress.value = true
  progressCurrent.value = 0
  progressTotal.value = ids.length
  let successCount = 0
  for (const id of ids) {
    const q = questions.value.find(q => q.id === id)
    if (q) {
      try {
        const { id: _id, createdAt, updatedAt, createdBy, ...rest } = q
        await questionsService.create({
          ...rest,
          questionBlocks: q.questionBlocks,
          options: q.options,
          hintBlocks: q.hintBlocks,
          explanationBlocks: q.explanationBlocks,
        })
        successCount++
      } catch {
        // continue
      }
    }
    progressCurrent.value++
    progressLabel.value = `Duplicating ${progressCurrent.value}/${progressTotal.value}...`
  }
  duplicatingInProgress.value = false
  try {
    ;[questions.value, folders.value] = await Promise.all([
      questionsService.list(),
      foldersService.list(),
    ])
    if (selectedFolderFilter.value === 'unfiled') {
      unfiledQuestions.value = [...questions.value]
        .filter(q => q.folderId == null)
        .sort((a, b) => a.orderIndex - b.orderIndex)
    } else if (typeof selectedFolderFilter.value === 'number') {
      folderQuestions.value = questions.value.filter(q => q.folderId === selectedFolderFilter.value)
        .sort((a, b) => a.orderIndex - b.orderIndex)
    }
  } catch {
    // ignore
  }
  clearSelection()
  if (successCount > 0) {
    toast.success(`${successCount} question(s) duplicated`)
  } else {
    toast.error('Failed to duplicate questions')
  }
}

async function moveSelectedToFolder() {
  const ids = Array.from(selectedQuestionIds.value)
  if (!ids.length) return
  movingInProgress.value = true
  progressCurrent.value = 0
  progressTotal.value = ids.length
  let successCount = 0
  const targetFolderId = moveToFolderTarget.value
  for (const id of ids) {
    try {
      await questionsService.assignFolder(id, targetFolderId)
      const q = questions.value.find(q => q.id === id)
      if (q) q.folderId = targetFolderId
      successCount++
    } catch {
      // continue
    }
    progressCurrent.value++
    progressLabel.value = `Moving ${progressCurrent.value}/${progressTotal.value}...`
  }
  movingInProgress.value = false
  if (selectedFolderFilter.value === 'unfiled' && targetFolderId !== null) {
    unfiledQuestions.value = [...questions.value]
      .filter(q => q.folderId == null)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  } else if (typeof selectedFolderFilter.value === 'number' && selectedFolderFilter.value !== targetFolderId) {
    folderQuestions.value = questions.value.filter(q => q.folderId === selectedFolderFilter.value)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  } else if (typeof selectedFolderFilter.value === 'number' && selectedFolderFilter.value === targetFolderId) {
    folderQuestions.value = questions.value.filter(q => q.folderId === targetFolderId)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  }
  moveToFolderModalVisible.value = false
  clearSelection()
  if (successCount > 0) {
    toast.success(`${successCount} question(s) moved`)
  } else {
    toast.error('Failed to move questions')
  }
}

// -- Utilities --------------------------------------------------------------
function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').trim()
}

function formatRelative(iso: string | null): string {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const secs = Math.floor(diff / 1000)
  if (secs < 60) return 'just now'
  const mins = Math.floor(secs / 60)
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  const days = Math.floor(hrs / 24)
  if (days < 30) return `${days}d ago`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}mo ago`
  return `${Math.floor(months / 12)}y ago`
}

// -- Questions --------------------------------------------------------------
const questions = ref<Question[]>([])
const loading = ref(true)
const loadError = ref(false)
const searchQuery = ref('')

// -- Folders ----------------------------------------------------------------
const folders = ref<Folder[]>([])
const selectedFolderFilter = ref<null | 'unfiled' | number>(null)

// Owned folders drive the VueDraggable model; shared folders are a static list below
const ownedFoldersModel = ref<Folder[]>([])
const sharedFolders = computed(() => folders.value.filter(f => f.role !== 'OWNER'))

// -- Folder pagination --------------------------------------------------------
const FOLDERS_PER_PAGE = 8
const folderPage = ref(1)
const ownedFolderPages = computed(() => Math.max(1, Math.ceil(ownedFoldersModel.value.length / FOLDERS_PER_PAGE)))
const paginatedOwnedFolders = computed(() => {
  const start = (folderPage.value - 1) * FOLDERS_PER_PAGE
  return ownedFoldersModel.value.slice(start, start + FOLDERS_PER_PAGE)
})

watch(folders, (newFolders) => {
  ownedFoldersModel.value = newFolders.filter(f => f.role === 'OWNER')
  folderPage.value = 1 // reset to first page on folder change
}, { immediate: true, deep: true })

// -- Pending invitations ----------------------------------------------------
const pendingInvitations = ref<FolderMember[]>([])
const invitationsExpanded = ref(true)

async function loadInvitations() {
  try {
    pendingInvitations.value = await foldersService.getInvitations()
  } catch {
    pendingInvitations.value = []
  }
}

async function acceptInvite(id: number) {
  try {
    await foldersService.acceptInvitation(id)
    pendingInvitations.value = pendingInvitations.value.filter(inv => inv.id !== id)
    ;[folders.value, questions.value] = await Promise.all([
      foldersService.list(),
      questionsService.list(),
    ])
    if (selectedFolderFilter.value === 'unfiled') {
      unfiledQuestions.value = [...questions.value]
        .filter(q => q.folderId == null)
        .sort((a, b) => a.orderIndex - b.orderIndex)
    }
  } catch {
    showAlert('Error', 'Failed to accept invitation.')
  }
}

async function declineInvite(id: number) {
  try {
    await foldersService.declineInvitation(id)
    pendingInvitations.value = pendingInvitations.value.filter(inv => inv.id !== id)
  } catch {
    showAlert('Error', 'Failed to decline invitation.')
  }
}

// -- Share modal ------------------------------------------------------------
const shareModalFolderId = ref<number | null>(null)
const shareModalFolderName = ref('')
const shareModalFolderOwner = ref('')
const shareModalReadonly = ref(false)

function openShareModal(folder: Folder) {
  shareModalFolderId.value = folder.id
  shareModalFolderName.value = folder.name
  shareModalFolderOwner.value = folder.createdBy
  shareModalReadonly.value = folder.role === 'CONTRIBUTOR'
}

function closeShareModal() {
  shareModalFolderId.value = null
}

// -- Permission helpers -----------------------------------------------------
function canDeleteQuestion(q: Question): boolean {
  if (q.createdBy === currentUsername.value) return true
  if (q.folderId == null) return false
  const folder = folders.value.find(f => f.id === q.folderId)
  if (!folder) return false
  return folder.role === 'OWNER' || folder.role === 'CO_OWNER'
}

const unfiledSorted = computed(() =>
  [...questions.value]
    .filter(q => q.folderId == null)
    .sort((a, b) => a.orderIndex - b.orderIndex)
)

const visibleQuestions = computed(() => {
  let list: Question[]
  if (selectedFolderFilter.value === null) {
    const folderQs = folders.value.flatMap(f =>
      questions.value.filter(q => q.folderId === f.id).sort((a, b) => a.orderIndex - b.orderIndex)
    )
    list = [...unfiledSorted.value, ...folderQs]
  } else if (selectedFolderFilter.value === 'unfiled') {
    list = unfiledSorted.value
  } else {
    list = questions.value.filter(q => q.folderId === selectedFolderFilter.value).sort((a, b) => a.orderIndex - b.orderIndex)
  }
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return list
  return list.filter(question => {
    const text = question.questionBlocks.find(b => b.type === 'text')?.content || ''
    return text.toLowerCase().includes(q)
  })
})

const folderCounts = computed(() => {
  const counts: Record<number, number> = {}
  for (const q of questions.value) {
    if (q.folderId != null) counts[q.folderId] = (counts[q.folderId] ?? 0) + 1
  }
  return counts
})

const unfiledCount = computed(() => questions.value.filter(q => q.folderId == null).length)

const currentFolderLabel = computed(() => {
  if (selectedFolderFilter.value === null) return 'Question Bank'
  if (selectedFolderFilter.value === 'unfiled') return 'Unfiled'
  return folders.value.find(f => f.id === selectedFolderFilter.value)?.name ?? 'Questions'
})

const activeFolderId = computed(() =>
  typeof selectedFolderFilter.value === 'number' ? selectedFolderFilter.value : null
)

// -- Per-folder question list (for drag reorder) ----------------------------
const folderQuestions = ref<Question[]>([])

watch(selectedFolderFilter, (newVal) => {
  if (typeof newVal === 'number') {
    folderQuestions.value = questions.value.filter(q => q.folderId === newVal)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  }
}, { immediate: true })

// -- Unfiled question list (for drag reorder) ------------------------------
const unfiledQuestions = ref<Question[]>([])

// Re-initialize with orderIndex sort whenever we switch TO the unfiled view
watch(selectedFolderFilter, (newVal) => {
  if (newVal === 'unfiled') {
    unfiledQuestions.value = [...questions.value]
      .filter(q => q.folderId == null)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  }
}, { immediate: true })

// -- Drag-to-folder ---------------------------------------------------------
const draggingQuestions = ref<Question[]>([])
const dropTarget = ref<'unfiled' | number | null>(null)

function getDraggableQuestions(q: Question): Question[] {
  if (selectedQuestionIds.value.has(q.id) && selectedQuestionIds.value.size > 1) {
    return visibleQuestions.value.filter(vq => selectedQuestionIds.value.has(vq.id))
  }
  return [q]
}

function onDragStart(qs: Question[], e: DragEvent) {
  draggingQuestions.value = qs
  e.dataTransfer?.setData('text/plain', qs.map(q => q.id).join(','))
  if (e.dataTransfer) e.dataTransfer.effectAllowed = 'move'
  if (qs.length > 1 && e.dataTransfer) {
    const img = new Image()
    e.dataTransfer.setDragImage(img, 0, 0)
  }
}

function onDragEnd() {
  draggingQuestions.value = []
  dropTarget.value = null
}

function onDragStartFolder(q: Question, e: DragEvent) {
  const path = e.composedPath() as Element[]
  if (path.some(el => (el as HTMLElement).classList?.contains('drag-handle'))) {
    const img = new Image()
    e.dataTransfer?.setDragImage(img, 0, 0)
    return
  }
  onDragStart(getDraggableQuestions(q), e)
}

function onDragOver(target: 'unfiled' | number) {
  if (!draggingQuestions.value.length) return
  dropTarget.value = target
}

function onDragLeave() {
  dropTarget.value = null
}

async function onDrop(target: 'unfiled' | number) {
  const questionsToMove = [...draggingQuestions.value]
  draggingQuestions.value = []
  dropTarget.value = null
  if (!questionsToMove.length) return
  const newFolderId = target === 'unfiled' ? null : (target as number)
  const toMove = questionsToMove.filter(q => q.folderId !== newFolderId)
  if (!toMove.length) return
  let movedCount = 0
  for (const q of toMove) {
    try {
      const updated = await questionsService.assignFolder(q.id, newFolderId)
      const idx = questions.value.findIndex(x => x.id === q.id)
      if (idx >= 0) questions.value.splice(idx, 1, updated)
      movedCount++
    } catch {
      // continue
    }
  }
  if (selectedFolderFilter.value === 'unfiled' && newFolderId !== null) {
    unfiledQuestions.value = unfiledQuestions.value.filter(x => !toMove.some(m => m.id === x.id))
  }
  if (typeof selectedFolderFilter.value === 'number' && selectedFolderFilter.value !== newFolderId) {
    folderQuestions.value = folderQuestions.value.filter(x => !toMove.some(m => m.id === x.id))
  }
  if (typeof selectedFolderFilter.value === 'number' && selectedFolderFilter.value === newFolderId) {
    const newlyMoved = questions.value.filter(q => toMove.some(m => m.id === q.id))
    folderQuestions.value.push(...newlyMoved)
  }
  if (movedCount > 0) {
    toast.success(`${movedCount} question(s) moved`)
  } else {
    showAlert('Error', 'Failed to move questions.')
  }
}

// -- Folder CRUD ------------------------------------------------------------
const creatingFolder = ref(false)
const newFolderName = ref('')
const newFolderInputRef = ref<HTMLInputElement | null>(null)

function startCreateFolder() {
  creatingFolder.value = true
  newFolderName.value = ''
  nextTick(() => newFolderInputRef.value?.focus())
}

async function submitCreateFolder() {
  const name = newFolderName.value.trim()
  creatingFolder.value = false
  newFolderName.value = ''
  if (!name) return
  try {
    const folder = await foldersService.create({ name })
    folders.value.push(folder)
    selectedFolderFilter.value = folder.id
  } catch {
    showAlert('Error', 'Failed to create folder.')
  }
}

function cancelCreateFolder() {
  creatingFolder.value = false
  newFolderName.value = ''
}

const editingFolderId = ref<number | null>(null)
const editingFolderName = ref('')
const folderInputRef = ref<HTMLInputElement | null>(null)

function startFolderRename(folder: Folder) {
  editingFolderId.value = folder.id
  editingFolderName.value = folder.name
  nextTick(() => folderInputRef.value?.focus())
}

async function submitFolderRename(id: number) {
  const name = editingFolderName.value.trim()
  editingFolderId.value = null
  if (!name) return
  const folder = folders.value.find(f => f.id === id)
  if (!folder || folder.name === name) return
  try {
    const updated = await foldersService.rename(id, { name })
    const idx = folders.value.findIndex(f => f.id === id)
    if (idx >= 0) folders.value.splice(idx, 1, updated)
  } catch {
    showAlert('Error', 'Failed to rename folder.')
  }
}

function cancelFolderRename() {
  editingFolderId.value = null
}

async function confirmDeleteFolder(folder: Folder) {
  const ok = await showConfirm(
    'Delete folder?',
    `"${folder.name}" will be deleted. Questions inside will become unfiled.`
  )
  if (!ok) return
  try {
    await foldersService.delete(folder.id)
    folders.value = folders.value.filter(f => f.id !== folder.id)
    questions.value.forEach(q => { if (q.folderId === folder.id) q.folderId = null })
    if (selectedFolderFilter.value === 'unfiled') {
      unfiledQuestions.value = [...questions.value]
        .filter(q => q.folderId == null)
        .sort((a, b) => a.orderIndex - b.orderIndex)
    }
    if (selectedFolderFilter.value === folder.id) selectedFolderFilter.value = null
  } catch {
    showAlert('Error', 'Failed to delete folder.')
  }
}

// -- Modal ------------------------------------------------------------------
const modalVisible = ref(false)
const editingQuestion = ref<Question | null>(null)

function openCreate() {
  editingQuestion.value = null
  modalVisible.value = true
}

function openEdit(q: Question) {
  editingQuestion.value = q
  modalVisible.value = true
}

function onSaved(q: Question) {
  const idx = questions.value.findIndex(x => x.id === q.id)
  if (idx >= 0) {
    questions.value = questions.value.map((x, i) => i === idx ? q : x)
    if (selectedFolderFilter.value === 'unfiled') {
      const uIdx = unfiledQuestions.value.findIndex(x => x.id === q.id)
      if (q.folderId == null) {
        if (uIdx >= 0) unfiledQuestions.value.splice(uIdx, 1, q)
        else unfiledQuestions.value.push(q)
      } else if (uIdx >= 0) {
        unfiledQuestions.value.splice(uIdx, 1)
      }
    } else if (typeof selectedFolderFilter.value === 'number') {
      const fIdx = folderQuestions.value.findIndex(x => x.id === q.id)
      if (q.folderId === selectedFolderFilter.value) {
        if (fIdx >= 0) folderQuestions.value.splice(fIdx, 1, q)
        else folderQuestions.value.push(q)
      } else if (fIdx >= 0) {
        folderQuestions.value.splice(fIdx, 1)
      }
    }
    toast.success('Question updated')
  } else {
    questions.value = [...questions.value, q]
    if (selectedFolderFilter.value === 'unfiled' && q.folderId == null) {
      unfiledQuestions.value.push(q)
    } else if (typeof selectedFolderFilter.value === 'number' && q.folderId === selectedFolderFilter.value) {
      folderQuestions.value.push(q)
    }
    toast.success('Question created')
  }
  modalVisible.value = false
}

async function duplicateQuestion(q: Question) {
  try {
    const { id, createdAt, updatedAt, createdBy, ...rest } = q
    const duplicated = await questionsService.create({
      ...rest,
      questionBlocks: q.questionBlocks,
      options: q.options,
      hintBlocks: q.hintBlocks,
      explanationBlocks: q.explanationBlocks,
    })
    questions.value.push(duplicated)
    if (selectedFolderFilter.value === 'unfiled' && duplicated.folderId == null) {
      unfiledQuestions.value.push(duplicated)
    } else if (typeof selectedFolderFilter.value === 'number' && duplicated.folderId === selectedFolderFilter.value) {
      folderQuestions.value.push(duplicated)
    }
    toast.success('Question duplicated')
  } catch {
    toast.error('Failed to duplicate question')
  }
}

// -- AppDialog --------------------------------------------------------------
const dialogVisible = ref(false)
const dialogType = ref<'confirm' | 'alert'>('alert')
const dialogTitle = ref('')
const dialogMessage = ref('')
let dialogResolve: ((ok: boolean) => void) | null = null

function showConfirm(title: string, message?: string): Promise<boolean> {
  dialogType.value = 'confirm'
  dialogTitle.value = title
  dialogMessage.value = message ?? ''
  dialogVisible.value = true
  return new Promise(resolve => { dialogResolve = resolve })
}

function showAlert(title: string, message?: string) {
  dialogType.value = 'alert'
  dialogTitle.value = title
  dialogMessage.value = message ?? ''
  dialogVisible.value = true
}

function onDialogConfirm() {
  dialogVisible.value = false
  dialogResolve?.(true)
  dialogResolve = null
}

function onDialogCancel() {
  dialogVisible.value = false
  dialogResolve?.(false)
  dialogResolve = null
}

// -- Load -------------------------------------------------------------------
onMounted(async () => {
  loading.value = true
  loadError.value = false
  try {
    ;[questions.value, folders.value] = await Promise.all([
      questionsService.list(),
      foldersService.list(),
    ])
    loadInvitations()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
})

// -- Delete question --------------------------------------------------------
async function confirmDelete(q: Question) {
  const label = q.questionBlocks.find(b => b.type === 'text')?.content || 'this question'
  const ok = await showConfirm('Delete question?', `"${label}" will be permanently removed.`)
  if (!ok) return
  try {
    await questionsService.delete(q.id)
    questions.value = questions.value.filter(x => x.id !== q.id)
    if (selectedFolderFilter.value === 'unfiled') {
      unfiledQuestions.value = unfiledQuestions.value.filter(x => x.id !== q.id)
    } else if (typeof selectedFolderFilter.value === 'number') {
      folderQuestions.value = folderQuestions.value.filter(x => x.id !== q.id)
    }
    toast.success('Question deleted')
  } catch {
    showAlert('Delete failed', 'Could not delete the question. Please try again.')
  }
}

// -- Reorder questions within a folder ------------------------------------
async function persistFolderQuestionsReorder() {
  const folderId = selectedFolderFilter.value as number
  // Merge the reordered folderQuestions back into the global questions array
  const newQuestions = [...questions.value]
  let fi = 0
  for (let i = 0; i < newQuestions.length; i++) {
    if (newQuestions[i].folderId === folderId) {
      newQuestions[i] = folderQuestions.value[fi++]
    }
  }
  // Update orderIndex to match new positions so sort-on-revisit is correct
  questions.value = newQuestions.map((q, i) => ({ ...q, orderIndex: i }))
  try {
    await questionsService.reorder(questions.value.map(q => q.id))
  } catch {
    try { questions.value = await questionsService.list() } catch { /* ignore */ }
  }
}

// -- Reorder unfiled questions ---------------------------------------------
async function persistUnfiledQuestionsReorder() {
  // Merge reordered unfiledQuestions back into global questions array
  const newQuestions = [...questions.value]
  let fi = 0
  for (let i = 0; i < newQuestions.length; i++) {
    if (newQuestions[i].folderId == null) {
      newQuestions[i] = unfiledQuestions.value[fi++]
    }
  }
  // Update orderIndex to match new positions so sort-on-revisit is correct
  questions.value = newQuestions.map((q, i) => ({ ...q, orderIndex: i }))
  try {
    await questionsService.reorder(questions.value.map(q => q.id))
  } catch {
    try { questions.value = await questionsService.list() } catch { /* ignore */ }
  }
}

// -- Reorder folders --------------------------------------------------------
async function persistFolderReorder() {
  try {
    await foldersService.reorder(ownedFoldersModel.value.map(f => f.id))
  } catch {
    /* order is cosmetic – silently ignore network failures */
  }
}

// -- Import handler ---------------------------------------------------------
async function onImported() {
  importModalVisible.value = false
  try {
    ;[questions.value, folders.value] = await Promise.all([
      questionsService.list(),
      foldersService.list(),
    ])
    if (selectedFolderFilter.value === 'unfiled') {
      unfiledQuestions.value = [...questions.value]
        .filter(q => q.folderId == null)
        .sort((a, b) => a.orderIndex - b.orderIndex)
    }
  } catch {
    toast.error('Failed to refresh questions')
  }
}
</script>

<style scoped>
.drag-ghost { opacity: 0.4; background: #dbeafe !important; }
</style>
