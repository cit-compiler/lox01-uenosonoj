package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//他のファイルをインポート
import static com.craftinginterpreters.lox.TokenType.*; 

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  //source:文章
  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      // We are at the beginning of the next lexeme.
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  //'"'まで読み進める
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;

      case '!':
      addToken(match('=') ? BANG_EQUAL : BANG);
      break;
      case '=':
      addToken(match('=') ? EQUAL_EQUAL : EQUAL);
      break;
      case '<':
      addToken(match('=') ? LESS_EQUAL : LESS);
      break;
      case '>':
      addToken(match('=') ? GREATER_EQUAL : GREATER);
      break;
      case '/':
      if (match('/')) {
        // A comment goes until the end of the line.
        while (peek() != '\n' && !isAtEnd()) advance();
      } else {
        addToken(SLASH);
      }
      break;
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;
      case '"': string(); break;

      default:
      if (isDigit(c)) {
        number();
      } else if (isAlpha(c)) {
        identifier();
      }else {
        Lox.error(line, "Unexpected character.");
      }
      break;
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void number() {
    while (isDigit(peek())) advance(); //数字の並びがくることがわかる

    // Look for a fractional part.'.'かつ後ろが数字だったら、'.'まで読み進める
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }
    //NUMBERという変数に数字の並びを代入する
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void string(){
    while (peek() != '"' && !isAtEnd()){
      if (peek() == '\n') line++;
      if(peek() == '\\' && peekNext() == '"'){
      advance();
      advance();
    }else if(peek() == '"'){
      break;
    }else{
      advance();
    }
      
    }
    //文字列おわってないのに閉じちゃったよ（文字列の閉じ忘れがあるよ）エラー
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
      return;
    }

    
    // The closing ".
    advance();  //'"'がある場所を確認

    // Trim the surrounding quotes.'"'を取り除いた文章を表示
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value.replace("\\\"","\""));
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  //currentを取り出す。文章終わりだったらNULLを返す
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  //current+1を取り出す
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  } 

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }


  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }
}

