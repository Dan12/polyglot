package jltools.ast;

/**
 * Class to handle statements such as ; or the statement inside of {};
 * This class makes the parser's job easier, as it doesn't have to sift
 * out the null's that would otherwise be generated by an empty statement.
 */
public interface Empty extends Stmt
{
}
