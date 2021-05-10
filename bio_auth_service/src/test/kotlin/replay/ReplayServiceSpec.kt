package replay

import io.kotest.core.spec.style.StringSpec
import io.ktor.util.KtorExperimentalAPI
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.kiva.bioauthservice.app.config.ReplayConfig
import org.kiva.bioauthservice.db.daos.ReplayDao
import org.kiva.bioauthservice.db.repositories.ReplayRepository
import org.kiva.bioauthservice.replay.ReplayService
import org.slf4j.LoggerFactory

@KtorExperimentalAPI
class ReplayServiceSpec : StringSpec({

    val logger = LoggerFactory.getLogger(this.javaClass)
    val mockConfig = mockk<ReplayConfig>()
    val mockReplayRepository = mockk<ReplayRepository>()
    val mockDao = mockk<ReplayDao>()
    val input = "foo".toByteArray()

    beforeEach {
        clearAllMocks()
    }

    "should check for replay attacks if replay attack protection is enabled" {
        every { mockConfig.enabled } returns true
        every { mockDao.countSeen } returns 1
        every { mockReplayRepository.addReplay(input) } returns mockDao
        val replayService = ReplayService(logger, mockReplayRepository, mockConfig)
        replayService.checkIfReplay(input)
        verify { mockReplayRepository.addReplay(input) }
    }

    "should not check for replay attacks if replay attack protection is disabled" {
        every { mockConfig.enabled } returns false
        val replayService = ReplayService(logger, mockReplayRepository, mockConfig)
        replayService.checkIfReplay(input)
        verify { mockReplayRepository wasNot Called }
    }
})
