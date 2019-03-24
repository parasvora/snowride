package cz.hudecekpetr.snowride.listener;

import cz.hudecekpetr.snowride.antlr.RobotBaseListener;
import cz.hudecekpetr.snowride.antlr.RobotParser;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.Lines;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.tree.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class AntlrListener extends RobotBaseListener implements ANTLRErrorListener {

    public List<Exception> errors = new ArrayList<>();

    @Override
    public void exitFile(RobotParser.FileContext ctx) {
        ctx.File = new RobotFile();
        for(RobotParser.SectionContext section : ctx.section()) {
            ctx.File.sections.add(section.Section);
        }
    }

    @Override
    public void exitSection(RobotParser.SectionContext ctx) {
        if (ctx.keywordsSection() != null) {
            ctx.Section = ctx.keywordsSection().Section;
        } else if (ctx.testCasesSection() != null) {
            ctx.Section = ctx.testCasesSection().Section;
        } else if (ctx.unknownSection() != null) {
            ctx.Section = ctx.unknownSection().Section;
        }
    }

    @Override
    public void exitUnknownSection(RobotParser.UnknownSectionContext ctx) {
        LogicalLine ll = new LogicalLine();
        ll.cells.add(new Cell("nondef",""));
        ctx.Section = new TextOnlyRobotSection(new SectionHeader(ll), "");
    }

    @Override
    public void exitTestCasesSection(RobotParser.TestCasesSectionContext ctx) {
        SectionHeader header = ctx.testCasesHeader().SectionHeader;
        if (ctx.emptyLines() != null) {
            header.followupEmptyLines = ctx.emptyLines().Trivia;
        }
        List<Scenario> tcc = ctx.testCase().stream().map(ctxx -> ctxx.TestCase).collect(Collectors.toList());
        ctx.Section = new TestCasesSection(header, tcc);
    }

    @Override
    public void exitTestCasesHeader(RobotParser.TestCasesHeaderContext ctx) {
        ctx.SectionHeader = new SectionHeader(SectionKind.TEST_CASES, ctx.getText());
    }

    @Override
    public void exitKeywordsHeader(RobotParser.KeywordsHeaderContext ctx) {
        ctx.SectionHeader = new SectionHeader(SectionKind.KEYWORDS, ctx.getText());
    }

    @Override
    public void exitTestCaseName(RobotParser.TestCaseNameContext ctx) {
        ctx.Cell = new Cell(ctx.ANY_CELL().getText(), ctx.restOfRow().getText());
    }

    @Override
    public void exitRestOfRow(RobotParser.RestOfRowContext ctx) {
        List<TerminalNode> spaces = ctx.CELLSPACE();
        List<TerminalNode> cells = ctx.ANY_CELL();
        LogicalLine line = new LogicalLine();
        for (int i =0; i < cells.size(); i++) {
            if (spaces.size() > i+1) {
                line.cells.add(new Cell(cells.get(i).getText(), spaces.get(i+1).getText()));
            } else {
                line.cells.add(new Cell(cells.get(i).getText(), ""));
            }
        }
        if (spaces.size() >= 1) {
            line.preTrivia = spaces.get(0).getText();
        }
        ctx.Line = line;
    }

    @Override
    public void exitTestCase(RobotParser.TestCaseContext ctx) {
        Cell nameCell = ctx.testCaseName().Cell;
        Lines settings = ctx.testCaseSettings().Lines;
        Lines steps = ctx.testCaseSteps().Lines;
        String emptyLines = ctx.emptyLines() != null ? ctx.emptyLines().Trivia : "";
        List<LogicalLine> newList = new ArrayList<LogicalLine>(settings.getLines());
        newList.addAll(steps.getLines());
        ctx.TestCase = new Scenario(nameCell, true, newList, emptyLines);
    }

    @Override
    public void exitStep(RobotParser.StepContext ctx) {
        ctx.LogicalLine = ctx.restOfRow().Line.prepend(ctx.CELLSPACE().getText(), ctx.ANY_CELL().getText());
    }

    @Override
    public void exitTestCaseSetting(RobotParser.TestCaseSettingContext ctx) {
        ctx.LogicalLine = ctx.restOfRow().Line.prepend(ctx.CELLSPACE().getText(), ctx.TEST_CASE_SETTING_CELL().getText());
    }

    @Override
    public void exitTestCaseSteps(RobotParser.TestCaseStepsContext ctx) {
        ctx.Lines = new Lines(ctx.step().stream().map(x -> x.LogicalLine).collect(Collectors.toList()));
    }

    @Override
    public void exitTestCaseSettings(RobotParser.TestCaseSettingsContext ctx) {
        ctx.Lines = new Lines(ctx.testCaseSetting().stream().map(x -> x.LogicalLine).collect(Collectors.toList()));
    }

    @Override
    public void exitKeywordsSection(RobotParser.KeywordsSectionContext ctx) {
        SectionHeader header = ctx.keywordsHeader().SectionHeader;
        StringBuilder text = new StringBuilder();
        if (ctx.emptyLines() != null) {
            header.followupEmptyLines = ctx.emptyLines().Trivia;
        }
        List<Scenario> tcc = ctx.testCase().stream().map(ctxx -> ctxx.TestCase).collect(Collectors.toList());
        tcc.forEach(sc->sc.setTestCase(false));
        ctx.Section = new KeywordsSection(header, tcc);
    }

    @Override
    public void exitEmptyLines(RobotParser.EmptyLinesContext ctx) {
        ctx.Trivia = ctx.getText();
    }

    //------------- errors
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        errors.add(e);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

    }
}
