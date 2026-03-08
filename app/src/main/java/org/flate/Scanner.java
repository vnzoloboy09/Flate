package org.flate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.HashMap;

import static org.flate.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("true", TRUE);
		keywords.put("for", FOR);
		keywords.put("while", WHILE);
		keywords.put("if", IF);
		keywords.put("null", NULL);
		keywords.put("return", RETURN);
		keywords.put("this", THIS);
		keywords.put("var", VAR);
		keywords.put("print", PRINT);
		keywords.put("func", FUNC);
		keywords.put("func", FUNC);
		keywords.put("super", SUPER);
		keywords.put("and", AND);
		keywords.put("or", OR);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while(!isAtEnd()) {
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private void scanToken() {
		char c = advance();

		switch(c) {
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
				if(match('/')) {
					while(peek() != '\n' && !isAtEnd()) {
						advance();
					}
				}
				else if(match('*')) {
					comment();
				}
				else {
					addToken(SLASH);
				}
				break;

			case ' ':
			case '\r':
			case '\t':
				break;

			case '\n': line++; break;

			case '"': string(); break;

			default:
				if(isDigit(c)) {
					number();
				}
				else if (isAlpha(c)) {
					identifier();
				}
				else {
					Flate.error(line, "Unexpected character.");
					break;
				}
		}
	}

	private void identifier() {
		while(isAlphaNumeric(peek())) {
			advance();
		}

		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if(type == null) {
			type = IDENTIFIER;
		}

		addToken(type);
	}

	private boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	private boolean isAlpha(char c) {
		return ('a' <= c && c <= 'z') ||
			   ('A' <= c && c <= 'Z') ||
			   (c == '_');
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private void number() {
		while(isDigit(peek())) {
			advance();
		}

		if(peek() == '.' && isDigit(peekNext())) {
			advance();
			while(isDigit(peek())) {
				advance();
			}
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private void comment() {
		int count = 1;
		while(!isAtEnd()) {
			char c = advance();
			if(c == '*' && match('/')) {
				count--;
				if(count == 0) break;
			}
			else if(c == '/' && match('*')) {
				count++;
			}
			if(c == '\n') {
				line++;
			}
		}

		if(isAtEnd()) {
			Flate.error(line, "Close comment expected.");
			return;
		}

		advance();
	}

	private void string() {
		while(peek() != '"' && !isAtEnd()) {
			if(peek() == '\n') {
				line++;
			}
			advance();
		}

		if(isAtEnd()) {
			Flate.error(line, "Unterminated string.");
			return;
		}

		advance();
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	private char peek() {
		if(isAtEnd()) {
			return '\0';
		}
		return source.charAt(current);
	}

	private char peekNext() {
		if(current + 1 >= source.length()) {
			return '\0';
		}
		return source.charAt(current + 1);
	}

	private boolean match(char expected) {
		if(isAtEnd()) {
			return false;
		}

		if(source.charAt(current) != expected) {
			return false;
		}

		current++;
		return true;
	}

	private char advance() {
		current++;
		return source.charAt(current - 1);
	}

	private void addToken(TokenType type) {
		addToken(type, null);
	}

	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
