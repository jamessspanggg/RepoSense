package reposense.authorship;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import reposense.authorship.model.FileInfo;
import reposense.authorship.model.FileResult;
import reposense.git.GitCheckout;
import reposense.git.exception.CommitNotFoundException;
import reposense.model.Author;
import reposense.model.CommitHash;
import reposense.template.GitTestTemplate;
import reposense.util.TestUtil;


public class FileAnalyzerTest extends GitTestTemplate {

    private static final Date BLAME_TEST_SINCE_DATE = TestUtil.getDate(2018, Calendar.FEBRUARY, 6);
    private static final Date BLAME_TEST_UNTIL_DATE = TestUtil.getDate(2018, Calendar.FEBRUARY, 8);
    private static final Date MOVED_FILE_SINCE_DATE = TestUtil.getDate(2018, Calendar.FEBRUARY, 7);
    private static final Date MOVED_FILE_UNTIL_DATE = TestUtil.getDate(2018, Calendar.FEBRUARY, 9);
    private static final Date PR_617_SINCE_DATE = TestUtil.getDate(2019, Calendar.MARCH, 28);
    private static final Date PR_617_UNTIL_DATE = TestUtil.getDate(2019, Calendar.MARCH, 29);

    @Test
    public void blameTest() {
        config.setSinceDate(BLAME_TEST_SINCE_DATE);
        config.setUntilDate(BLAME_TEST_UNTIL_DATE);
        FileResult fileResult = getFileResult("blameTest.java");
        assertFileAnalysisCorrectness(fileResult);
    }

    @Test
    public void movedFileBlameTest() {
        config.setSinceDate(MOVED_FILE_SINCE_DATE);
        config.setUntilDate(MOVED_FILE_UNTIL_DATE);
        FileResult fileResult = getFileResult("newPos/movedFile.java");
        assertFileAnalysisCorrectness(fileResult);
    }

    @Test
    public void blameTestDateRange() throws CommitNotFoundException {
        Date sinceDate = BLAME_TEST_SINCE_DATE;
        Date untilDate = BLAME_TEST_UNTIL_DATE;

        GitCheckout.checkoutDate(config.getRepoRoot(), config.getBranch(), untilDate);
        config.setSinceDate(sinceDate);
        config.setUntilDate(untilDate);

        FileResult fileResult = getFileResult("blameTest.java");
        assertFileAnalysisCorrectness(fileResult);
    }

    @Test
    public void movedFileBlameTestDateRange() throws CommitNotFoundException {
        Date sinceDate = MOVED_FILE_SINCE_DATE;
        Date untilDate = MOVED_FILE_UNTIL_DATE;

        GitCheckout.checkoutDate(config.getRepoRoot(), config.getBranch(), untilDate);
        config.setSinceDate(sinceDate);
        config.setUntilDate(untilDate);

        FileResult fileResult = getFileResult("newPos/movedFile.java");
        assertFileAnalysisCorrectness(fileResult);
    }

    @Test
    public void analyzeFile_blameTestFileIgnoreFakeAuthorCommitFullHash_success() {
        config.setSinceDate(BLAME_TEST_SINCE_DATE);
        config.setUntilDate(BLAME_TEST_UNTIL_DATE);

        FileInfo fileInfoFull = generateTestFileInfo("blameTest.java");
        config.setIgnoreCommitList(Collections.singletonList(FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018));
        FileInfoAnalyzer.analyzeFile(config, fileInfoFull);

        FileInfo fileInfoShort = generateTestFileInfo("blameTest.java");
        config.setIgnoreCommitList(
                Collections.singletonList(
                        new CommitHash(FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018_STRING.substring(0, 8))));
        FileInfoAnalyzer.analyzeFile(config, fileInfoShort);

        Assert.assertEquals(fileInfoFull, fileInfoShort);

        Assert.assertEquals(new Author(MAIN_AUTHOR_NAME), fileInfoFull.getLine(1).getAuthor());
        Assert.assertEquals(new Author(MAIN_AUTHOR_NAME), fileInfoFull.getLine(2).getAuthor());
        Assert.assertEquals(new Author(MAIN_AUTHOR_NAME), fileInfoFull.getLine(4).getAuthor());

        // line added in commit that was ignored
        Assert.assertEquals(Author.UNKNOWN_AUTHOR, fileInfoFull.getLine(3).getAuthor());
    }

    @Test
    public void analyzeFile_blameTestFileIgnoreAllCommit_success() {
        config.setSinceDate(BLAME_TEST_SINCE_DATE);
        config.setUntilDate(BLAME_TEST_UNTIL_DATE);

        FileInfo fileInfoFull = generateTestFileInfo("blameTest.java");
        config.setIgnoreCommitList(Arrays.asList(FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018,
                MAIN_AUTHOR_BLAME_TEST_FILE_COMMIT_06022018));
        FileInfoAnalyzer.analyzeFile(config, fileInfoFull);

        FileInfo fileInfoShort = generateTestFileInfo("blameTest.java");
        config.setIgnoreCommitList(CommitHash.convertStringsToCommits(
                Arrays.asList(FAKE_AUTHOR_BLAME_TEST_FILE_COMMIT_08022018_STRING.substring(0, 8),
                        MAIN_AUTHOR_BLAME_TEST_FILE_COMMIT_06022018_STRING.substring(0, 8))));
        FileInfoAnalyzer.analyzeFile(config, fileInfoShort);

        Assert.assertEquals(fileInfoFull, fileInfoShort);
        fileInfoFull.getLines().forEach(lineInfo ->
                Assert.assertEquals(Author.UNKNOWN_AUTHOR, lineInfo.getAuthor()));
    }

    @Test
    public void analyzeFile_emailWithAdditionOperator_success() {
        config.setSinceDate(PR_617_SINCE_DATE);
        config.setUntilDate(PR_617_UNTIL_DATE);

        config.setBranch("617-FileAnalyzerTest-analyzeFile_emailWithAdditionOperator_success");
        GitCheckout.checkoutBranch(config.getRepoRoot(), config.getBranch());
        Author author = new Author(MINGYI_AUTHOR_NAME);
        config.setAuthorList(Collections.singletonList(author));

        FileInfo fileInfo = FileInfoExtractor.generateFileInfo(config.getRepoRoot(), "pr_617.java");
        FileInfoAnalyzer.analyzeFile(config, fileInfo);

        Assert.assertEquals(1, fileInfo.getLines().size());
        fileInfo.getLines().forEach(lineInfo -> Assert.assertEquals(author, lineInfo.getAuthor()));
    }
}
