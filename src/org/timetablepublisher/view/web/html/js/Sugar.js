//
// SUGAR FUNCTION -- add method concept to all classes
// http://www.crockford.com/javascript/
Function.prototype.method = function (name, func)
{
    this.prototype[name] = func;
    return this;
};
