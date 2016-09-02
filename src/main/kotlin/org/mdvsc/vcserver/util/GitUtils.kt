package org.mdvsc.vcserver.util

import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.dircache.DirCacheBuilder
import org.eclipse.jgit.dircache.DirCacheEntry
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevWalk
import org.mdvsc.vcserver.server.PathProvider
import org.mdvsc.vcserver.server.ServerHelper
import java.io.File

/**
 * @author haniklz
 * @since 16/4/1.
 */
object GitUtils {

    val masterRef = "refs/heads/master"
    val readmeFileName = "README.md"
    val ignoreFileName = ".gitIgnore"

    private fun insertDirCacheEntry(objectInserter: ObjectInserter, name: String, content: ByteArray) = DirCacheEntry(name).apply {
        length = content.size
        lastModified = System.currentTimeMillis()
        fileMode = FileMode.REGULAR_FILE
        setObjectId(objectInserter.insert(Constants.OBJ_BLOB, content))
    }

    fun commitContent(repository: Repository
                              , ref: String
                              , userName: String
                              , userEmail: String
                              , commitMessage: String
                              , builder: ((inserter: ObjectInserter, indexBuilder: DirCacheBuilder) -> Unit)? = null
    ): Boolean {
        val objectInserter = repository.newObjectInserter()
        val user = PersonIdent(userName, userEmail)
        val newIndex = DirCache.newInCore()
        val indexBuilder = newIndex.builder();
        builder?.invoke(objectInserter, indexBuilder)
        val treeId = newIndex.writeTree(objectInserter)
        val commitId = objectInserter.insert(CommitBuilder().apply {
            author = user
            committer = user
            message = commitMessage
            encoding = ServerHelper.defaultCharset
            setTreeId(treeId)
        })
        objectInserter.flush()

        val revWalk = RevWalk(repository)
        val revCommit = revWalk.parseCommit(commitId)
        return (repository.updateRef(ref).apply {
            refLogIdent = user
            setNewObjectId(commitId)
            setRefLogMessage("commit: ${revCommit.shortMessage}", false)
        }.update() == RefUpdate.Result.NEW).apply {
            revWalk.release()
            objectInserter.release()
        }
    }

    private fun insertFile(inserter: ObjectInserter, builder: DirCacheBuilder, file: File, name: String = file.name) {
        if (file.exists() && file.isFile && file.canRead()) {
            builder.add(insertDirCacheEntry(inserter, name, file.readBytes()))
        }
    }

    fun commitFiles(repository: Repository
                    , ref: String
                    , userName: String
                    , userEmail: String
                    , commitMessage: String
                    , vararg files: File?) = files.size > 0 && commitContent(repository, ref, userName, userEmail, commitMessage
            , { inserter, builder -> files.forEach { if (it != null && it.isFile && it.canRead()) insertFile(inserter, builder, it) } })

    fun createRepository(repoPathFile: File, name:String, email:String, content:String, readmeFile:File? = null, ignoreFile:File? = null) = RepositoryBuilder().apply {
        setBare()
        gitDir = repoPathFile
    }.build().apply {
        create(true)
        commitFiles(this, masterRef, name, email, content, readmeFile, ignoreFile)
    }

    fun buildRepository(pathProvider: PathProvider, repoName: String) = RepositoryBuilder().apply {
        setBare()
        isMustExist = false
        gitDir = pathProvider.getPathFile(PathProvider.Folder.REPO, repoName)
    }.build()
}

