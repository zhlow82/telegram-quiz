<template>
  <AppLayout>
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

          <!-- Owned folder list (draggable) -->
          <VueDraggable
            v-model="ownedFoldersModel"
            handle=".folder-drag-handle"
            :animation="150"
            ghost-class="drag-ghost"
            @end="persistFolderReorder"
          >
          <div v-for="folder in ownedFoldersModel" :key="folder.id">
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

        <!-- Page header -->
        <div class="flex items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
          <div class="flex items-center gap-4">
            <div class="w-10 h-10 rounded-xl bg-blue-600 flex items-center justify-center flex-shrink-0">
              <BookOpen class="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 class="text-2xl font-black text-slate-900 leading-tight">{{ currentFolderLabel }}</h1>
              <p class="text-sm text-slate-500 mt-0.5">
                {{ visibleQuestions.length }} question{{ visibleQuestions.length !== 1 ? 's' : '' }}
                <template v-if="typeof selectedFolderFilter === 'number' || selectedFolderFilter === 'unfiled'"> � drag to reorder</template>
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
              class="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition cursor-pointer"
              @click="openCreate"
            >
              <Plus class="w-4 h-4" />
              Add Question
            </button>
          </div>
        </div>

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
              <span class="block text-sm font-medium text-slate-900 truncate sm:whitespace-normal">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
              <div class="flex gap-1.5 mt-1 flex-wrap">
                <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                  <BookOpen class="w-3 h-3" />Briefing
                </span>
                <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                  <Type class="w-3 h-3" />Text Input
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
            </div>
            <div class="flex items-center gap-1.5 flex-shrink-0">
              <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
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
              :class="{ 'opacity-50': draggingQuestion?.id === q.id }"
              draggable="true"
              @dragstart="onDragStartFolder(q, $event)"
              @dragend="onDragEnd"
            >
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
                <span class="block text-sm font-medium text-slate-900 truncate sm:whitespace-normal">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap">
                  <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                    <BookOpen class="w-3 h-3" />Briefing
                  </span>
                  <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                    <Type class="w-3 h-3" />Text Input
                  </span>
                  <span v-else-if="q.options.length" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-blue-100 text-blue-700">
                    <ListChecks class="w-3 h-3" />Multiple Choice
                  </span>
                  <span v-else-if="q.expectPhoto" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-violet-100 text-violet-700">
                    <Camera class="w-3 h-3" />Photo
                  </span>
                  <span v-if="!q.isBriefing && q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-slate-100 text-slate-500">{{ q.options.length }} options</span>
                </div>
              </div>
              <div class="flex items-center gap-1.5 flex-shrink-0">
                <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
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
              :class="{ 'opacity-50': draggingQuestion?.id === q.id }"
              draggable="true"
              @dragstart="onDragStartFolder(q, $event)"
              @dragend="onDragEnd"
            >
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
                <span class="block text-sm font-medium text-slate-900 truncate sm:whitespace-normal">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap">
                  <span v-if="q.isBriefing" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-amber-100 text-amber-700">
                    <BookOpen class="w-3 h-3" />Briefing
                  </span>
                  <span v-else-if="q.expectsTextInput" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-emerald-100 text-emerald-700">
                    <Type class="w-3 h-3" />Text Input
                  </span>
                  <span v-else-if="q.options.length" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-blue-100 text-blue-700">
                    <ListChecks class="w-3 h-3" />Multiple Choice
                  </span>
                  <span v-else-if="q.expectPhoto" class="inline-flex items-center gap-1 text-[0.6875rem] font-semibold px-2 py-0.5 rounded-md bg-violet-100 text-violet-700">
                    <Camera class="w-3 h-3" />Photo
                  </span>
                  <span v-if="!q.isBriefing && q.options.length" class="inline-flex items-center text-[0.6875rem] font-medium px-2 py-0.5 rounded-md bg-slate-100 text-slate-500">{{ q.options.length }} options</span>
                </div>
              </div>
              <div class="flex items-center gap-1.5 flex-shrink-0">
                <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 shrink-0">
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
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { VueDraggable } from 'vue-draggable-plus'
import {
  Plus, AlertCircle, BookOpen, GripVertical, Pencil, Trash2,
  Type, ListChecks, Camera,
  Library, Inbox, FolderPlus, FolderOpen, Share2, Bell, ChevronDown,
  Folder as FolderIcon, FolderSymlink, Search, Copy,
} from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import QuestionFormModal from '@/components/QuestionFormModal.vue'
import AppDialog from '@/components/AppDialog.vue'
import FolderMembersModal from '@/components/FolderMembersModal.vue'
import { questionsService } from '@/services/questionsService'
import { foldersService } from '@/services/foldersService'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import type { Question } from '@/types/question'
import type { Folder, FolderMember } from '@/types/folder'

const authStore = useAuthStore()
const toast = useToast()
const currentUsername = computed(() => authStore.username ?? '')

// -- Utilities --------------------------------------------------------------
function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').trim()
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
watch(folders, (newFolders) => {
  ownedFoldersModel.value = newFolders.filter(f => f.role === 'OWNER')
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
    // Reload folders and questions to pick up the newly shared folder
    ;[folders.value, questions.value] = await Promise.all([
      foldersService.list(),
      questionsService.list(),
    ])
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

watch([selectedFolderFilter, questions], () => {
  if (typeof selectedFolderFilter.value === 'number') {
    folderQuestions.value = questions.value.filter(q => q.folderId === selectedFolderFilter.value)
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

// Re-sync only when the unfiled count changes (add / delete / move to-from folder).
// A same-count change means it was a drag reorder � preserve the user's order.
watch(questions, (newVal) => {
  if (selectedFolderFilter.value !== 'unfiled') return
  const newCount = newVal.filter(q => q.folderId == null).length
  if (newCount !== unfiledQuestions.value.length) {
    unfiledQuestions.value = [...newVal]
      .filter(q => q.folderId == null)
      .sort((a, b) => a.orderIndex - b.orderIndex)
  }
})

// -- Drag-to-folder ---------------------------------------------------------
const draggingQuestion = ref<Question | null>(null)
const dropTarget = ref<'unfiled' | number | null>(null)

function onDragStart(q: Question, e: DragEvent) {
  draggingQuestion.value = q
  e.dataTransfer?.setData('text/plain', String(q.id))
  if (e.dataTransfer) e.dataTransfer.effectAllowed = 'move'
}

function onDragEnd() {
  draggingQuestion.value = null
  dropTarget.value = null
}

function onDragStartFolder(q: Question, e: DragEvent) {
  const path = e.composedPath() as Element[]
  if (path.some(el => (el as HTMLElement).classList?.contains('drag-handle'))) {
    // Drag from grip handle � SortableJS handles reordering.
    // Suppress the browser's ghost image so it doesn't double-up with SortableJS's clone.
    const img = new Image()
    e.dataTransfer?.setDragImage(img, 0, 0)
    return  // Don't set draggingQuestion; sidebar drop zones stay inactive
  }
  onDragStart(q, e)
}

function onDragOver(target: 'unfiled' | number) {
  if (!draggingQuestion.value) return
  dropTarget.value = target
}

function onDragLeave() {
  dropTarget.value = null
}

async function onDrop(target: 'unfiled' | number) {
  const q = draggingQuestion.value
  draggingQuestion.value = null
  dropTarget.value = null
  if (!q) return
  const newFolderId = target === 'unfiled' ? null : (target as number)
  if (q.folderId === newFolderId) return
  try {
    const updated = await questionsService.assignFolder(q.id, newFolderId)
    const idx = questions.value.findIndex(x => x.id === q.id)
    if (idx >= 0) questions.value.splice(idx, 1, updated)
  } catch {
    showAlert('Error', 'Failed to move question.')
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
    toast.success('Question updated')
  } else {
    questions.value = [...questions.value, q]
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
    /* order is cosmetic � silently ignore network failures */
  }
}
</script>

<style scoped>
.drag-ghost { opacity: 0.4; background: #dbeafe !important; }
</style>